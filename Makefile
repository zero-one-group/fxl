DOCKERNAME=zeroonetechnology/fxl
VERSION=`cat resources/FXL_REPL_RELEASED_VERSION`

build:
	cp project.clj docker/project.clj
	docker build -f docker/Dockerfile \
		-t $(DOCKERNAME):latest \
		docker

docker-pull:
	docker pull $(DOCKERNAME):$(VERSION)

docker-push: build
	docker push $(DOCKERNAME):latest

docker-release: build
	cp project.clj docker/project.clj
	docker build -f docker/Dockerfile \
		-t $(DOCKERNAME):$(VERSION) \
		docker
	docker push $(DOCKERNAME):$(VERSION)

dock: build
	docker run --rm -v $(PWD):/root/fxl -w /root/fxl -it $(DOCKERNAME) \
		/bin/bash

repl: build
	docker run --rm -v $(PWD):/root/fxl -w /root/fxl -it $(DOCKERNAME) \
		lein repl

coverage: build
	$(eval TMP := $(shell mktemp -d))
	cp -r . $(TMP)
	docker run --rm -v $(TMP):/root/fxl -w /root/fxl -t $(DOCKERNAME) \
		scripts/coverage

lint-ancient: build
	$(eval TMP := $(shell mktemp -d))
	cp -r . $(TMP)
	docker run --rm -v $(TMP):/root/fxl -w /root/fxl -t $(DOCKERNAME) \
		scripts/lint-ancient

ci: coverage lint-ancient
	echo "CI steps passed!"
