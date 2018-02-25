package de.guysoft;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import de.guysoft.javadsl.service.MediaFileManager;
import de.guysoft.javadsl.service.UploadService;
import de.guysoft.javadsl.service.UploadServiceImpl;

public class UploadServiceModule extends AbstractModule implements ServiceGuiceSupport {

    @Override
    protected void configure() {
        bindService(UploadService.class, UploadServiceImpl.class);
        bind(MediaFileManager.class);
    }
}
