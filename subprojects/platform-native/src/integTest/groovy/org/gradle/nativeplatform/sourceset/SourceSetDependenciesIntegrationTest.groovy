/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.gradle.nativeplatform.sourceset

import org.gradle.nativeplatform.fixtures.AbstractInstalledToolChainIntegrationSpec
import org.gradle.nativeplatform.fixtures.app.CHelloWorldApp
import org.gradle.nativeplatform.fixtures.app.CppCallingCHelloWorldApp

// TODO:DAZ Test incremental
// TODO:DAZ Test dependency on functional source set
// TODO:DAZ Test dependency on source set that is not HeaderExportingSourceSet
// TODO:DAZ Sad day tests
class SourceSetDependenciesIntegrationTest extends AbstractInstalledToolChainIntegrationSpec {

    def "source dependency on source set of same type"() {
        def app = new CHelloWorldApp()
        app.executable.writeSources(file("src/main"))
        app.library.writeSources(file("src/library"))

        buildFile << """
    apply plugin: 'c'

    executables {
        main {
            sources {
                library(CSourceSet) {
                    source.srcDir "src/library/c"
                    exportedHeaders.srcDir "src/library/headers"
                }
                c.lib sources.library
            }
        }
    }
"""
        when:
        succeeds "mainExecutable"

        then:
        executable("build/binaries/mainExecutable/main").exec().out == app.englishOutput
    }

    def "source dependency on source set of headers"() {
        def app = new CHelloWorldApp()
        app.executable.writeSources(file("src/main"))
        app.library.sourceFiles*.writeToDir(file("src/main"))
        app.library.headerFiles*.writeToDir(file("src/library"))

        buildFile << """
    apply plugin: 'c'

    libraries {
        library
    }

    executables {
        main {
            sources {
                c.lib libraries.library.sources.c
            }
        }
    }
"""
        when:
        succeeds "mainExecutable"

        then:
        executable("build/binaries/mainExecutable/main").exec().out == app.englishOutput
    }

    def "source dependency on source set of different type"() {
        def app = new CppCallingCHelloWorldApp()
        app.executable.writeSources(file("src/main"))
        app.library.writeSources(file("src/library"))

        buildFile << """
    apply plugin: 'cpp'
    apply plugin: 'c'

    executables {
        main {
            sources {
                library(CSourceSet) {
                    exportedHeaders.srcDir "src/library/headers"
                    source.srcDir "src/library/c"
                }
                cpp.lib sources.library
            }
        }
    }

"""
        when:
        succeeds "mainExecutable"

        then:
        executable("build/binaries/mainExecutable/main").exec().out == app.englishOutput
    }

    def "source files in depended-on source set are not included"() {
        given:
        def app = new CHelloWorldApp()
        app.writeSources(file("src/main"))

        file("src/extra/cpp/bad.cpp") << """
    FILE WILL BE IGNORED: source set dependency set only considers headers
"""

        buildFile << """
    apply plugin: 'cpp'

    libraries {
        extra
    }

    executables {
        main {
            sources {
                cpp.lib libraries.extra.sources.cpp
            }
        }
    }
"""
        expect:
        succeeds "mainExecutable"
    }

    def "binary depending on source set has no effect"() {
        given:
        def app = new CHelloWorldApp()
        app.writeSources(file("src/main"))

        file("src/extra/headers/hello.h") << """
    FILE WILL BE IGNORED: source set dependency added to binary
"""

        buildFile << """
    apply plugin: 'cpp'

    libraries {
        extra
    }

    executables {
        main {
            binaries.all {
                lib libraries.extra.sources.cpp
            }
        }
    }
"""
        expect:
        succeeds "mainExecutable"
    }
}