param(
    [ValidateSet('auto', '1.21.1', '26.1')]
    [string]$Target = 'auto'
)

$ErrorActionPreference = 'Stop'

function Get-ProjectRoot {
    return Split-Path -Parent $PSScriptRoot
}

function Get-Utf8NoBomEncoding {
    return New-Object System.Text.UTF8Encoding($false)
}

function Read-Text([string]$Path) {
    return [System.IO.File]::ReadAllText($Path, [System.Text.Encoding]::UTF8)
}

function Write-Text([string]$Path, [string]$Content) {
    $directory = Split-Path -Parent $Path
    if ($directory -and -not (Test-Path $directory)) {
        New-Item -ItemType Directory -Path $directory | Out-Null
    }
    [System.IO.File]::WriteAllText($Path, $Content, (Get-Utf8NoBomEncoding))
}

function Resolve-Target([string]$RequestedTarget, [string]$ProjectRoot) {
    if ($RequestedTarget -ne 'auto') {
        return $RequestedTarget
    }

    $gradlePropertiesPath = Join-Path $ProjectRoot 'gradle.properties'
    if (-not (Test-Path $gradlePropertiesPath)) {
        throw "Cannot resolve target automatically: gradle.properties was not found."
    }

    $minecraftVersion = $null
    foreach ($line in (Get-Content $gradlePropertiesPath)) {
        if ($line -match '^minecraft_version=(.+)$') {
            $minecraftVersion = $Matches[1].Trim()
            break
        }
    }

    switch ($minecraftVersion) {
        '1.21.1' { return '1.21.1' }
        '26.1' { return '26.1' }
        default { throw "Unsupported minecraft_version '$minecraftVersion' in gradle.properties." }
    }
}

$presets = @{
    '1.21.1' = @{
        MinecraftVersion = '1.21.1'
        GradleVersion = '8.14.3'
        JavaVersion = '21'
        GradleJvm = 'temurin-21'
        ProjectJdkName = '21'
        LanguageLevel = 'JDK_21'
        BytecodeTarget = '21'
    }
    '26.1' = @{
        MinecraftVersion = '26.1'
        GradleVersion = '9.1.0'
        JavaVersion = '25'
        GradleJvm = 'temurin-25'
        ProjectJdkName = '25'
        LanguageLevel = 'JDK_25'
        BytecodeTarget = '25'
    }
}

$projectRoot = Get-ProjectRoot
$effectiveTarget = Resolve-Target -RequestedTarget $Target -ProjectRoot $projectRoot
$preset = $presets[$effectiveTarget]

$wrapperPath = Join-Path $projectRoot 'gradle\wrapper\gradle-wrapper.properties'
$gradleXmlPath = Join-Path $projectRoot '.idea\gradle.xml'
$miscXmlPath = Join-Path $projectRoot '.idea\misc.xml'
$compilerXmlPath = Join-Path $projectRoot '.idea\compiler.xml'

if (Test-Path $wrapperPath) {
    $wrapperText = Read-Text $wrapperPath
    $expectedDistribution = "distributionUrl=https\://services.gradle.org/distributions/gradle-$($preset.GradleVersion)-bin.zip"
    if ($wrapperText -match 'distributionUrl=https\\://services\.gradle\.org/distributions/gradle-[0-9.]+-bin\.zip') {
        $wrapperText = [System.Text.RegularExpressions.Regex]::Replace(
            $wrapperText,
            'distributionUrl=https\\://services\.gradle\.org/distributions/gradle-[0-9.]+-bin\.zip',
            $expectedDistribution
        )
    }
    else {
        $wrapperText = $wrapperText.TrimEnd() + "`r`n" + $expectedDistribution + "`r`n"
    }
    Write-Text $wrapperPath $wrapperText
}

$gradleXml = @"
<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="GradleMigrationSettings" migrationVersion="1" />
  <component name="GradleSettings">
    <option name="linkedExternalProjectsSettings">
      <GradleProjectSettings>
        <option name="externalProjectPath" value="`$PROJECT_DIR$" />
        <option name="gradleJvm" value="$($preset.GradleJvm)" />
        <option name="modules">
          <set>
            <option value="`$PROJECT_DIR$" />
          </set>
        </option>
        <option name="resolveExternalAnnotations" value="true" />
      </GradleProjectSettings>
    </option>
  </component>
</project>
"@
Write-Text $gradleXmlPath $gradleXml

$miscXml = @"
<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="ExternalStorageConfigurationManager" enabled="true" />
  <component name="FrameworkDetectionExcludesConfiguration">
    <file type="web" url="file://`$PROJECT_DIR$" />
  </component>
  <component name="ProjectRootManager" version="2" languageLevel="$($preset.LanguageLevel)" default="true" project-jdk-name="$($preset.ProjectJdkName)" project-jdk-type="JavaSDK" />
</project>
"@
Write-Text $miscXmlPath $miscXml

$compilerXml = @"
<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="CompilerConfiguration">
    <bytecodeTargetLevel target="$($preset.BytecodeTarget)" />
  </component>
</project>
"@
Write-Text $compilerXmlPath $compilerXml

Write-Host "Workspace switched to preset $effectiveTarget" -ForegroundColor Green
Write-Host "  Minecraft: $($preset.MinecraftVersion)"
Write-Host "  Gradle:    $($preset.GradleVersion)"
Write-Host "  Java:      $($preset.JavaVersion)"
Write-Host ""
Write-Host "Updated files:"
Write-Host "  gradle/wrapper/gradle-wrapper.properties"
Write-Host "  .idea/gradle.xml"
Write-Host "  .idea/misc.xml"
Write-Host "  .idea/compiler.xml"
Write-Host ""
Write-Host "Next step: reload the Gradle project in IntelliJ IDEA if it is already open."
