package org.example;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Advanced Tests Suite")
@SelectPackages("org.example")
@IncludeTags("advanced")
public class AdvancedSuite {
}