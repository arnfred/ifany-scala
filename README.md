The scala code behind www.ifany.org

If you're browsing this code, please keep in mind that while I occasionally update parts, most of it is almost a decade old.

# Dev Setup
To run the code, install sbt, decrypt the environment and run sbt with the environment passed in: 

```
brew install sbt
gpg --decrypt < aws.test.env.gpg > aws.test.env
env $(cat aws.test.env) sbt
```

Inside SBT you can type `run` or `compile` to either run or compile the code.

Deploying to dokku
------------------

Occasionally, the build either can't reserve sufficient memory or stalls when deploying to dokku. This is a bit stupid and it's mostly because I'm building my artefacts on the same small host that runs a bunch of web apps because I'm stingy and don't want to pay for a bigger one. Usually the solution is to just stop the current `photos` instance from running:

```
dokku ps:stop photos
```

This will mean a short amount of downtime, which I'm not at all concerned about.
