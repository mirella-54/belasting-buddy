FROM eclipse-temurin:21-jre

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    tesseract-ocr \
    tesseract-ocr-eng \
    libtesseract-dev \
    libpng-dev \
    libjpeg-dev \
    libtiff-dev \
    libleptonica-dev && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

ENV TESSDATA_PREFIX /usr/share/tesseract-ocr/5

WORKDIR /app

COPY target/doc-processing-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]