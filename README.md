# Workshop Registration System – EX2 Maven Evolution

## Structure

```
root                (aggregator + top parent)
|-- core            (aggregator + parent of the implementation modules)
|   |-- model-impl  implementation of the model interfaces
|   `-- logic-impl  WorkshopSystem implementation (JSON export, e-mail validation, logging)
`-- app             workshop-cli: demo Main, builds the uber jar workshop-cli.jar
```

## 1. Install the API and tester jars (once per computer)

The jars are not on Maven Central. From the folder that holds them, run:

```sh
mvn install:install-file -Dfile=model-2.0.jar -DgroupId=patmal.workshops -DartifactId=model -Dversion=2.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=logic-2.0.jar -DgroupId=patmal.workshops -DartifactId=logic -Dversion=2.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=workshop-tester-2.0.jar -DgroupId=patmal.workshops -DartifactId=workshop-tester -Dversion=2.0 -Dpackaging=jar -DgeneratePom=true
```

`-DgeneratePom=true` matters: the API jars carry an embedded pom whose parent
(`workshops-aggregator`) does not exist, so Maven would fail to resolve them.

## 2. Build

```sh
mvn clean install
```

The uber jar is `app/target/workshop-cli.jar`.

The build also runs the tester in the `verify` phase (exec-maven-plugin, app module), so the
tester jar from step 1 must be installed. A failing tester fails the build.

## 3. Run

```sh
java -jar app/target/workshop-cli.jar                 # demo students from the bundled students.csv
java -jar app/target/workshop-cli.jar my-students.csv # demo students from your own CSV
```

A CSV has the header `id,name,email`, any ids, and at least 3 students (otherwise the app exits
with an error and exports nothing). The last student stays free; the others are split into
team Alpha (Java Workshop) and team Beta (Web Workshop). The run writes `instructors.json`, `students.json` and `teams.json` to the current folder.

## 4. Test with the tester

```sh
java -cp "workshop-tester-2.0.jar:app/target/workshop-cli.jar" patmal.workshops.main.Main   # macOS / Linux
java -cp "workshop-tester-2.0.jar;app/target/workshop-cli.jar" patmal.workshops.main.Main   # Windows
```

## Libraries

| Library | Module / scope | Why |
|---|---|---|
| Log4j 2 (`log4j-api`, versions via `log4j-bom`) | logic-impl, app – compile | logging API used in code |
| Log4j 2 (`log4j-core`) | app – runtime | logging implementation, only needed to run |
| Gson | logic-impl – compile | JSON exports |
| commons-validator | logic-impl – compile | e-mail validation |
| commons-csv | logic-impl – compile | CSV import (bonus) |
| workshop-tester | app – test | run by the build, not packed into the uber jar |

`commons-validator` brings `commons-beanutils`, `commons-digester`, `commons-logging` and
`commons-collections` transitively, `commons-csv` brings `commons-io` and `commons-codec`, and
Gson brings `error_prone_annotations` (see `mvn dependency:tree`).
