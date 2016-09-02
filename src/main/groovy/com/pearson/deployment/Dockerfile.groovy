package com.pearson.deployment

import com.pearson.deployment.helpers.Helper
import com.pearson.deployment.config.bitesize.*

import java.util.regex.*

import java.util.Date
import java.text.DateFormat
import java.text.SimpleDateFormat

class Dockerfile implements Serializable {
  String filename
  Application application
  String dockerRegistry

  private writer

  Dockerfile(Application app) {
    this.application = app
    this.dockerRegistry = Helper.dockerRegistry()
  }

  def contents() {
    def entrypoint = commandToEntrypoint(application.command)

    def dependencies = ""
    application.dependencies?.each {
      dependencies += it.version ? " ${it.name}=${it.version}-* " : "${it.name} "
    }

    """\
       FROM ${this.dockerRegistry}/baseimages/${application.runtime}
       MAINTAINER Bitesize Project <bitesize-techops@pearson.com>
       RUN echo 'deb http://apt/ bitesize main' > /etc/apt/sources.list.d/bitesize.list
       RUN apt-get -q update && \
           apt-get install -y --force-yes ${dependencies} && \
           rm -rf /var/cache/apt           
       ENTRYPOINT [${entrypoint}]
    """.stripIndent()
  }

  String currentTimeTag() {
    new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
  }

  private def commandToEntrypoint(String cmd) {
    String regex = "\"([^\"]*)\"|(\\S+)"
    Matcher m = Pattern.compile(regex).matcher(cmd);
    def commands = []
    while (m.find()) {
      if (m.group(1) != null ) {
        commands.push "\"${m.group(1)}\"".toString()
      } else {
        commands.push "\"${m.group(2)}\"".toString()
      }
    }
    commands.join(",") ?: 'bash'
  }
}
