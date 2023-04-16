FROM openjdk:11-jre

COPY ./group8-sep/out/artifacts/group8_sep_jar/group8-sep.jar .
COPY info.csv .
COPY Amenities.synonym .
COPY config.csv .
COPY cityData.csv .

EXPOSE 4567
EXPOSE 443

CMD ["java", "-jar", "group8-sep.jar"]
