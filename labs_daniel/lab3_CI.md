## 7. Continuous Integration

### What is CI and how was it applied?

Continuous Integration (CI) is the practice of automatically building and testing code 
every time a change is pushed to the repository. The goal is to catch integration errors 
early — before they reach the main branch — by running the full build and test suite on 
every push or pull request without manual intervention.

In this project, CI was set up using **GitHub Actions**. A workflow file was added at 
`.github/workflows/maven.yml` that triggers automatically on every push and pull request 
to the `main` branch. The pipeline runs on a clean `ubuntu-latest` runner, sets up 
JDK 17, builds the project with Maven, and then executes all tests.

The workflow file:

```yaml
name: Java CI with Maven

on:
  push:
    branches: [ "main" ]
  pull_request:
    branches: [ "main" ]

permissions:
  contents: read
  packages: read

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
          cache: maven

      - name: Configure Maven settings
        run: |
          mkdir -p ~/.m2
          cp .maven-settings.xml ~/.m2/settings.xml

      - name: Build project
        run: mvn -B clean package --file pom.xml
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}

      - name: Execute tests automatically
        run: mvn -B test --file pom.xml
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

### Maven settings

Because the project depends on shared JARs hosted on GitHub Packages 
(from `sweat-tek/MavenRepository`), a `.maven-settings.xml` file was added to the 
project root. The workflow copies this file to `~/.m2/settings.xml` before building, 
authenticating with GitHub Packages using the repository's built-in `GITHUB_TOKEN` secret.

### How git was used

Changes were developed on a dedicated feature branch (`feature/align-refactoring`) 
and submitted as a pull request to `main`. This triggered the CI pipeline automatically, 
confirming the build passed and all tests — including the new `AlignActionTest` — 
were green before merging. This ensures the refactoring did not break any existing 
functionality.