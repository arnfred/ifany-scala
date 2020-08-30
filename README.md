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
