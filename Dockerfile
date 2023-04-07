FROM openjdk:11-jre

COPY out/artifacts/group8_sep_jar/group8-sep.jar .
COPY info.csv .

EXPOSE 4567
EXPOSE 443

CMD ["java", "-jar", "group8-sep.jar"]
