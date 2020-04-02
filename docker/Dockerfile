FROM clojure:lein-2.9.1

ENV USER root
ENV HOME /root

ADD project.clj /root/project.clj
RUN cd /root && lein with-profile +dev deps

RUN wget -O /root/joker.zip https://github.com/candid82/joker/releases/download/v0.15.0/joker-0.15.0-linux-amd64.zip
RUN unzip /root/joker.zip -d /root \
    && mv /root/joker /usr/local/bin \
    && rm /root/joker.zip

RUN bash -c "bash <(curl -s https://raw.githubusercontent.com/borkdude/clj-kondo/master/script/install-clj-kondo)"

ENTRYPOINT []
