package com.alexhqi.saveshare.service.git;

import com.alexhqi.saveshare.service.RemoteSaveService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class GitSaveService implements RemoteSaveService {

    public static final String SERVICE_ID = "GIT_LOCAL_SAVE_SERVICE";
    private static final String DATA_DIRECTORY = ".saveshare/gitservice";
    private static final String REPO_DIRECTORY = DATA_DIRECTORY + "/repo";
    private static final String CONFIG_FILE = DATA_DIRECTORY + "/configuration.txt";
    private static final String REPO_INFO_FILE = "info.txt";

    private static final Logger LOGGER = LogManager.getLogger(GitSaveService.class);

    private ServiceConfiguration serviceConfiguration;
    private File configFile;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Git> gitMap = new HashMap<>();

    public List<GitSave> getSaves() {
        return new ArrayList<>(serviceConfiguration.getSaves());
    }

    public List<GitRepo> getRepos() {
        return new ArrayList<>(serviceConfiguration.getRepos());
    }

    // todo break out the GitRepo specific methods into a separate GitRepoService

    @Override
    public String getId() {
        return SERVICE_ID;
    }

    @Override
    public boolean initialize() throws Exception {
        LOGGER.info("Initializing GitSaveService");
        initConfiguration();
        initGitRepos();
        return true;
    }

    private void initConfiguration() throws IOException {
        LOGGER.info("Init config.");
        configFile = getConfigFile();
        serviceConfiguration = readObjectFromFile(configFile.toPath(), ServiceConfiguration.class);
        LOGGER.info("Configuration loaded.");
    }

    // should move this to a Util class
    private <T> T readObjectFromFile(Path path, Class<T> clazz) throws IOException {
        StringBuilder sb = new StringBuilder();
        Files.readAllLines(path).forEach(sb::append);
        if (sb.length() > 0) {
            ObjectReader reader = objectMapper.readerFor(clazz);
            return reader.readValue(sb.toString());
        } else {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("Class " + clazz.getCanonicalName() + " does not offer an accessible default constructor.");
            }
        }
    }

    private void initGitRepos() throws IOException, GitAPIException {
        LOGGER.info("Init local repo.");
        Path pathToLocalRepo = getAppDataDirectory().resolve(REPO_DIRECTORY);
        if (!pathToLocalRepo.toFile().exists() && !pathToLocalRepo.toFile().mkdirs()) {
            throw new IllegalStateException("Failed to create local repo in " +pathToLocalRepo.toString());
        }

        // todo hard reset each repo - clearing misc files, resetting to remote state

        serviceConfiguration.getSaves().clear();
        for (GitRepo repo : serviceConfiguration.getRepos()) {
            Path repoPath = getRepoPath(repo);
            Repository gitRepository = new FileRepositoryBuilder()
                    .setWorkTree(repoPath.toFile())
                    .setGitDir(repoPath.resolve(".git").toFile())
                    .setMustExist(true)
                    .setup()
                    .build();

            // todo, validate that git repo is in clean state

            Git gitInstance = new Git(gitRepository);
            gitMap.put(repo.getName(), gitInstance);
            PullResult result = pullUpdates(repo);
            LOGGER.info(result.toString());

            List<GitSave> repoSaves = getRepoInfo(repo).getSaves();
            for (GitSave repoSave : repoSaves) {
                repoSave.setRepoName(repo.getName());
            }
            serviceConfiguration.getSaves().addAll(repoSaves);
        }
    }

    private RepoInfo getRepoInfo(GitRepo repo) throws IOException {
        File repoInfoFile = getRepoInfoPath(repo).toFile();
        if (repoInfoFile.exists()) {
            return readObjectFromFile(repoInfoFile.toPath(), RepoInfo.class);
        } else {
            return new RepoInfo();
        }
    }

    private Path getRepoInfoPath(GitRepo repo) {
        return getRepoPath(repo).resolve(REPO_INFO_FILE);
    }

    private Path getAppDataDirectory() {
        return Paths.get(System.getenv("APPDATA"));
    }

    public void addRepo(GitRepo repo) throws IOException, GitAPIException {
        // todo enforce GitRepo name uniqueness
        File repoDirectory = getRepoPath(repo).toFile();
        if (!repoDirectory.exists() && !repoDirectory.mkdir()) {
            throw new IllegalStateException("Failed to create local repo directory with name: " + repo.getName());
        }

        Git.cloneRepository()
                .setURI(repo.getRepoUri().toString())
                .setDirectory(repoDirectory)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(repo.getToken(), ""))
                .call();

        serviceConfiguration.getRepos().add(repo);
        saveServiceConfigurationToFile();
        initGitRepos(); // pulls updates for all, and reads in Save Catalogues
    }

    private void saveServiceConfigurationToFile() throws IOException {
        writeObjectToFile(serviceConfiguration, ServiceConfiguration.class, configFile.toPath());
    }

    private <T> void writeObjectToFile(T object, Class<T> clazz, Path path) throws IOException {
        Files.write(path, objectMapper.writerFor(clazz).writeValueAsString(object).getBytes());
    }

    private Path getRepoPath(GitRepo repo) {
        return getAppDataDirectory().resolve(REPO_DIRECTORY).resolve(repo.getName());
    }

    public void removeRepo(String name) throws IOException {
        Optional<GitRepo> repoOptional = serviceConfiguration.getRepos().stream().filter(repo -> repo.getName().equals(name)).findFirst();
        if (repoOptional.isPresent()) {
            GitRepo repo = repoOptional.get();
            if (!serviceConfiguration.getRepos().remove(repo)) {
                throw new RuntimeException("Failed to remove repo from GitSaveService configuration.");
            }
            // try to save ahead of time in case any file stuff goes wrong,
            // at least the directory will be left alone
            saveServiceConfigurationToFile();

            gitMap.get(repo.getName()).close();
            gitMap.remove(repo.getName());
            File repoDirectory = getRepoPath(repo).toFile();
            if (repoDirectory.exists()) {
                // todo this seems to fail regularly on the .git files..
                FileUtils.deleteDirectory(repoDirectory);
            }
        }
    }

    private PullResult pullUpdates(GitRepo repoInfo) throws GitAPIException {
        Git git = gitMap.get(repoInfo.getName());
        return git.pull()
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(repoInfo.getToken(), ""))
                .setRemoteBranchName("master")
                .call();
    }

    private File getConfigFile() throws IOException {
        LOGGER.info("Seeking configuration file.");
        Path pathToConfigFile = getConfigPath();
        if (!pathToConfigFile.getParent().toFile().exists() && !pathToConfigFile.getParent().toFile().mkdirs()) {
            throw new IllegalStateException("Failed to create necessary directories for GitSaveService config file in " + pathToConfigFile.toString());
        }

        File configFile = pathToConfigFile.toFile();
        if (!configFile.exists()) {
            LOGGER.info("No existing configuration found. Generating in {}", pathToConfigFile);
            if (!configFile.createNewFile()) {
                throw new IllegalStateException("Failed to create new configuration file.");
            }
        }
        return configFile;
    }

    private Path getConfigPath() {
        return getAppDataDirectory().resolve(CONFIG_FILE);
    }

    @Override
    public File getSaveWithId(UUID remoteSaveUuid) {
        Optional<GitSave> optionalSave = serviceConfiguration.getSaves().stream()
                .filter((gitSave -> gitSave.getUuid().equals(remoteSaveUuid)))
                .findFirst();
        if (optionalSave.isPresent()) {
            GitSave save = optionalSave.get();
            Optional<GitRepo> optionalRepo = serviceConfiguration.getRepos().stream()
                    .filter((gitRepo -> gitRepo.getName().equals(save.getRepoName())))
                    .findFirst();
            if (optionalRepo.isEmpty()) {
                LOGGER.warn("Could not find the associated GitRepo for save with id: {}", remoteSaveUuid);
                return null;
            }
            GitRepo repo = optionalRepo.get();
            try {
                pullUpdates(repo);
                // could be a directory or a single file.
                File saveFile = getRepoPath(repo).resolve(save.getRepoPath()).toFile();
                if (!saveFile.exists()) {
                    LOGGER.error("Save file specified by remote save id {} does not exist.", remoteSaveUuid);
                    return null;
                }
                return saveFile;
            } catch (GitAPIException e) {
                LOGGER.error("Exception encountered loading Save File with id: {}", remoteSaveUuid, e);
                return null;
            }
        }

        LOGGER.warn("Could not find GitSave reference for id: {}", remoteSaveUuid);
        return null;
    }

    @Override
    public void updateSaveWithId(UUID remoteSaveUuid) {
        Optional<GitSave> optionalSave = serviceConfiguration.getSaves().stream()
                .filter((save) -> save.getUuid().equals(remoteSaveUuid))
                .findFirst();
        if (optionalSave.isPresent()) {
            GitSave gitSave = optionalSave.get();
            Optional<GitRepo> optionalRepo = serviceConfiguration.getRepos().stream()
                    .filter(repo -> repo.getName().equals(gitSave.getRepoName()))
                    .findFirst();
            if (optionalRepo.isPresent()) {
                GitRepo repo = optionalRepo.get();
                try {
                    pushUpdatedSaveFile(repo, gitSave);
                } catch (GitAPIException e) {
                    LOGGER.error("Exception encountered updating save with id {}", remoteSaveUuid, e);
                    throw new RuntimeException("Git Exception encountered updating save with id " + remoteSaveUuid, e);
                }
            } else {
                LOGGER.error("Could not find repo to update save with id {}", remoteSaveUuid);
                throw new IllegalStateException("Could not find repo to update save with id: " + remoteSaveUuid);
            }
        } else {
            LOGGER.warn("Attempted to update unknown save with id {}", remoteSaveUuid);
        }
    }

    @Override
    public List<UUID> getAllSaves() {
        return serviceConfiguration.getSaves().stream().map(GitSave::getUuid).collect(Collectors.toList());
    }

    public void uploadSaveFile(File save, GitRepo repo) throws GitAPIException, IOException {
        if (save == null || !save.exists()) {
            throw new IllegalArgumentException("The given File does not exist.");
        }
        // room for a race case here with someone else updating the remote after updates are pulled but before push
        pullUpdates(repo);

        UUID uuid = UUID.randomUUID();
        Path repoPath = Paths.get(uuid.toString(), save.getName());
        Path dest = getRepoPath(repo).resolve(repoPath);
        // existence check here wouldn't hurt

        GitSave gitSave = new GitSave();
        gitSave.setName(save.getName());
        gitSave.setRepoName(repo.getName());
        gitSave.setRepoPath(repoPath.toString());
        gitSave.setUuid(uuid);

        if (save.isDirectory()) {
            FileUtils.copyDirectory(save, dest.toFile());
        } else {
            FileUtils.copyFile(save, dest.toFile());
        }

        pushUpdatedSaveFile(repo, gitSave);
        addSaveToRepoInfo(repo, gitSave);
        initGitRepos();
    }

    private void pushUpdatedSaveFile(GitRepo repo, GitSave gitSave) throws GitAPIException {
        Git git = gitMap.get(repo.getName());
        if (git == null) {
            throw new IllegalStateException("The given GitRepo has no associated Git instance.");
        }
        git.add().addFilepattern(gitSave.getUuid().toString() + "/").call();
        git.commit().setMessage(getUpdateMessage(gitSave)).call();
        for (PushResult result : git.push()
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(repo.getToken(), ""))
                .call()) {
            for (RemoteRefUpdate update : result.getRemoteUpdates()) {
                if (RemoteRefUpdate.Status.OK != update.getStatus()) {
                    throw new RuntimeException("Failed to push GitSave " + gitSave.getUuid() + " to remote source.");
                }
            }
        }
    }

    private void addSaveToRepoInfo(GitRepo repo, GitSave gitSave) throws IOException, GitAPIException {
        RepoInfo info = getRepoInfo(repo);
        info.getSaves().add(gitSave);
        writeObjectToFile(info, RepoInfo.class, getRepoInfoPath(repo));

        Git git = gitMap.get(repo.getName());
        if (git == null) {
            throw new IllegalStateException("The given GitRepo has no associated Git instance.");
        }
        git.add().addFilepattern(REPO_INFO_FILE).call();
        git.commit().setMessage(getNewSaveMessage(gitSave)).call();
        for (PushResult result : git.push()
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(repo.getToken(), ""))
                .call()) {
            for (RemoteRefUpdate update : result.getRemoteUpdates()) {
                if (RemoteRefUpdate.Status.OK != update.getStatus()) {
                    throw new RuntimeException("Failed to push RepoInfo update to remote source for repo " + repo);
                }
            }
        }
    }

    private String getUpdateMessage(GitSave save) {
        return "AutoCommit " + new Date() + "\n\n" +
                "Name: " + save.getName() + "\n" +
                "UUID: " + save.getUuid();
    }

    private String getNewSaveMessage(GitSave save) {
        return "Repo Info " + new Date() + "\n\n" +
                "Save Added" + "\n" +
                "Name: " + save.getName() + "\n" +
                "UUID: " + save.getUuid();

    }


}
