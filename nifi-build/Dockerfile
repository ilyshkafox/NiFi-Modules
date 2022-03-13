ARG IMAGE_NAME=openjdk
ARG IMAGE_TAG=11-jre
ARG NIFI_IMAGE_NAME=apache/nifi
ARG NIFI_IMAGE_TAG=1.15.3

FROM ${NIFI_IMAGE_NAME}:${NIFI_IMAGE_TAG} AS original



FROM ${IMAGE_NAME}:${IMAGE_TAG}  AS artifactbase
ARG MAINTAINER="Apache NiFi Fork"
LABEL maintainer="${MAINTAINER}"

ARG UID=1000
ARG GID=1000

ENV NIFI_BASE_DIR /opt/nifi
ENV NIFI_HOME ${NIFI_BASE_DIR}/nifi-current
ENV NIFI_TOOLKIT_HOME ${NIFI_BASE_DIR}/nifi-toolkit-current
ENV NIFI_PID_DIR=${NIFI_HOME}/run
ENV NIFI_LOG_DIR=${NIFI_HOME}/logs

# Setup NiFi user and create necessary directories
RUN groupadd -g ${GID} nifi || groupmod -n nifi `getent group ${GID} | cut -d: -f1` \
    && useradd --shell /bin/bash -u ${UID} -g ${GID} -m nifi \
    && apt-get update \
    && apt-get install -y jq xmlstarlet procps

COPY --chown=nifi:nifi --from=original $NIFI_BASE_DIR $NIFI_BASE_DIR
COPY --chown=nifi:nifi  drivers $NIFI_HOME/drivers
COPY --chown=nifi:nifi  target/lib/*.nar $NIFI_HOME/lib/
COPY --chown=nifi:nifi  jolt $NIFI_HOME/jolt

VOLUME ${NIFI_LOG_DIR} \
       ${NIFI_HOME}/conf \
       ${NIFI_HOME}/database_repository \
       ${NIFI_HOME}/flowfile_repository \
       ${NIFI_HOME}/content_repository \
       ${NIFI_HOME}/provenance_repository \
       ${NIFI_HOME}/state

USER nifi

# Clear nifi-env.sh in favour of configuring all environment variables in the Dockerfile
RUN echo "#!/bin/sh\n" > $NIFI_HOME/bin/nifi-env.sh

# Web HTTP(s) & Socket Site-to-Site Ports
EXPOSE 8080 8443 10000 8000

WORKDIR ${NIFI_HOME}

# Apply configuration and start NiFi
#
# We need to use the exec form to avoid running our command in a subshell and omitting signals,
# thus being unable to shut down gracefully:
# https://docs.docker.com/engine/reference/builder/#entrypoint
#
# Also we need to use relative path, because the exec form does not invoke a command shell,
# thus normal shell processing does not happen:
# https://docs.docker.com/engine/reference/builder/#exec-form-entrypoint-example
ENTRYPOINT ["../scripts/start.sh"]