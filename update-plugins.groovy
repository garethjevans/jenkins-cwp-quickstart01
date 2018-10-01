@Grab('org.yaml:snakeyaml:1.17')

import groovy.json.*
import static groovy.io.FileType.FILES
import org.yaml.snakeyaml.Yaml

String.metaClass.isLaterVersionThan = { String version -> 
    List verA = version.tokenize('.')
    List verB = delegate.tokenize('.')

    def commonIndices = Math.min(verA.size(), verB.size())

    for (int i = 0; i < commonIndices; ++i) {
      	def numA = verA[i].toInteger()
      	def numB = verB[i].toInteger()

      	if (numA != numB) {
        	return numA < numB
      	}
    }

    return verA.size() < verB.size()
}

assert '4'.isLaterVersionThan('2')
assert '4.1'.isLaterVersionThan('4')
assert '5'.isLaterVersionThan('4.1')
assert '02.2.02.03'.isLaterVersionThan('02.2.02.01')
assert '2.60.2'.isLaterVersionThan('1.625.3')

assert !'2'.isLaterVersionThan('4')
assert !'4'.isLaterVersionThan('4.1')
assert !'4.1'.isLaterVersionThan('5')
assert !'02.2.02.01'.isLaterVersionThan('02.2.02.03')
assert !'1.625.3'.isLaterVersionThan('2.60.2')

Yaml parser = new Yaml()
def config = parser.load(("packager-config.yml" as File).text)
def jenkinsVersion = config.war.source.version

def plugins = [:]
config.plugins.each{ plugins.put( it.artifactId, it.source.version ) }

def content = "https://updates.jenkins.io/current/update-center.actual.json".toURL().text

def jsonSlurper = new JsonSlurper()
def meta = jsonSlurper.parseText(content)
	
meta.plugins.each { k,v -> 
   	if (plugins.containsKey(k)) {
       	if (!(v.requiredCore.isLaterVersionThan(jenkinsVersion))) {
           	def newVersion = v.version 
           	def currentVersion = plugins[k]
           	if (newVersion != currentVersion) {
               	println "${k} ${currentVersion} -> ${newVersion}"
               	plugins[k] = newVersion
           	}
            
           	//v.dependencies.findAll{ !it.optional }.each {
               	//if (!plugins.containsKey(it.name)) {
                   	//println "Missing dependency - ${it.name}:${it.version}"
                   	//plugins.put(it.name, it.version)
               	//}
           	//}
       	} else {
           	println "Unable to update ${k}, requires core ${v.requiredCore}"
       	}
   	}
}
