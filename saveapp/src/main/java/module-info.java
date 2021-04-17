module SaveShare.saveapp {
    requires com.fasterxml.jackson.databind;
    requires org.apache.logging.log4j;
    requires org.apache.commons.io;
    requires org.eclipse.jgit;
    requires slf4j.api;
    exports com.alexhqi.saveapp.core;
    exports com.alexhqi.saveapp.service;
    exports com.alexhqi.saveapp.service.git;
}