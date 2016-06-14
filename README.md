# xkcd-analysis

run directly using gradle wrapper or build fat jar

* ./gradlew bootRun
* or
* ./gradlew bootRepackage

check values for 

* PATH (where files are downloaded to, default "/tmp/digital.perservation/")
* BASE_URL (xkcd domain)
* START_URL (starting point, default "/1/")
* MAX_DEPTH (number of images to be downloaded)
