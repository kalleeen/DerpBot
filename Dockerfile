FROM maven:3-openjdk-8

CMD ["/bin/sh"]

RUN mkdir /derpbot-build
WORKDIR /derpbot-build
COPY . /derpbot-build
RUN mvn clean package

RUN mkdir /derpbot
RUN cp /derpbot-build/target/DerpBot.jar /derpbot/
RUN rm -r /derpbot-build
WORKDIR /derpbot

CMD java -jar DerpBot.jar