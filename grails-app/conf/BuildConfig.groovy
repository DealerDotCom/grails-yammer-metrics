/*
 * Copyright 2012 Jeff Ellis
 */

private String profile = System.getProperty("profile", "dev")

private List<String> globalExcludes = [
        'slf4j',
        'slf4j-api',
        'slf4j-log4j12',
        'log4j',
        'jcl-over-slf4j',
        'spring-amqp',
        'spring-rabbit',
        'jackson-mapper-asl',
        'jackson-core-asl',
        'http-builder',
        'xalan',
        'xml-apis',
        'groovy',
        'xercesImpl',
        'nekohtml',
        'xmlrpc-common',
        'xmlrpc-client',
        'ws-commons-util',
        'framework-common'
]

private def addGlobalExcludes = { added = null ->
    def toExclude = added ? (globalExcludes + added) : globalExcludes
    return toExclude.toArray()
}

private def getExcludesFor = { List modulesToInclude, added = null ->
    def toExclude = added ? (globalExcludes + added) : globalExcludes
    toExclude.removeAll(modulesToInclude)
    return toExclude.toArray()
}


grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

yammermetrics.version = "2.1.2"

grails.project.dependency.resolution = {
    useOrigin(true)

    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }



    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'

    repositories {

        grailsRepo "http://grails.org/plugins"

        grailsPlugins()
        grailsHome()
        grailsCentral()

        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        mavenLocal()
        mavenCentral()

    }

    plugins {
//		build ':release:2.0.3', {
//            export = false
//        }
        build(":release:1.0.0.RC3") {
            export = false
        }

    }

    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        compile("com.yammer.metrics:metrics-core:${yammermetrics.version}") {
            excludes(addGlobalExcludes())
        }
        compile("com.yammer.metrics:metrics-servlet:${yammermetrics.version}") {
            excludes(addGlobalExcludes())
        }
        compile("com.yammer.metrics:metrics-graphite:${yammermetrics.version}") {
            excludes(addGlobalExcludes())
        }

    }
}

def getDefaultDeployRepo(profileName){
    switch(profileName){
        case 'dev':
            return 'ddcDevelopment'
        case 'qa':
            return 'ddcQa'
        case 'production':
            return 'ddcReleases'
    }
    return null
}

grails.project.repos.ddcDevelopment.url = "http://maven.dev.dealer.ddc/content/repositories/development"
grails.project.repos.ddcQa.url = "http://maven.dev.dealer.ddc/content/repositories/candidate/"
grails.project.repos.ddcReleases.url = "http://maven.dev.dealer.ddc/content/repositories/releases"
grails.project.repos.ddcSnapshots.url = "http://maven.dev.dealer.ddc/content/repositories/snapshots"
grails.project.repos.default = getDefaultDeployRepo(profile)
grails.release.scm.enabled = false
