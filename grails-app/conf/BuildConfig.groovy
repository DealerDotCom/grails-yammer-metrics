String profile = System.getProperty("profile", "dev")

List<String> globalExcludesList = [
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
        'groovy-all',
        'xercesImpl',
        'nekohtml',
        'xmlrpc-common',
        'xmlrpc-client',
        'ws-commons-util',
        'framework-common',
        'quartz',
        'release',
        'mx4j-jmx',
        'mx4j-remote',
        'mx4j'
]

String[] globalExcludes = globalExcludesList.toArray()

grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

yammermetrics.version = "2.1.2"

grails.project.dependency.resolution = {

    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }

    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'

    repositories {
        mavenRepo "http://maven.dev.dealer.ddc/content/groups/production"
        mavenRepo "http://maven.dev.dealer.ddc/content/groups/public"

        grailsPlugins()
        grailsHome()
        grailsCentral()
    }

    plugins {
        build ':release:2.0.3', {
            export = false
        }
    }

    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        compile("com.yammer.metrics:metrics-core:${yammermetrics.version}") {
            excludes globalExcludes
        }
        compile("com.yammer.metrics:metrics-servlet:${yammermetrics.version}") {
            excludes globalExcludes
        }
        compile("com.yammer.metrics:metrics-graphite:${yammermetrics.version}") {
            excludes globalExcludes
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
