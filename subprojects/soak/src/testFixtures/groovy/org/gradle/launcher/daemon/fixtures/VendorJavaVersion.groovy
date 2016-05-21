/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.launcher.daemon.fixtures

import org.gradle.api.JavaVersion

class VendorJavaVersion implements Comparable<VendorJavaVersion> {
    JdkVendor vendor
    String version

    @Override
    int compareTo(VendorJavaVersion o) {
        int vendorComparison = vendor.compareTo(o.vendor)
        if (vendorComparison == 0) {
            return JavaVersion.toVersion(version).compareTo(JavaVersion.toVersion(o.version))
        } else {
            return vendorComparison
        }
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        VendorJavaVersion that = (VendorJavaVersion) o

        if (vendor != that.vendor) return false
        if (version != that.version) return false

        return true
    }

    int hashCode() {
        int result
        result = (vendor != null ? vendor.hashCode() : 0)
        result = 31 * result + version.hashCode()
        return result
    }

    @Override
    public String toString() {
        return "${vendor} ${version}";
    }
}
