
## Mandatory - Removed Compilation Errors

The project does not compile. To be able to compile the project the class
de.guysoft.scaladsl.service.UploadServiceImpl should be changed to be able to compile first.


## UploadService Java

In the class de.guysoft.javadsl.service.UploadServiceImpl compiles successfully and works well.


## UploadService Scala

This has a compilation error in the class de.guysoft.scaladsl.service.UploadServiceImpl.


## Issue

Lagom 1.3 missing MessageSerializer for play.api.mvc.Result in the Scala DSL.


## Run UploadService Java

* Create a directory for media file
* Change the directory for media file in the application.conf with the new created one
* Start service - sbt runAll
* Upload file - curl -i -X POST -H "Content-Type: multipart/form-data" -F "data=@/path/to/file.jpg" localhost:9000/java/service/media/users/1/upload
* View the uploaded file - extract the location header of the previous curl command and put it in the browser. It should look like this
localhost:9000/java/service/media/users/1/medias/762ab8f2-15b0-4513-88ac-2c12d6082702