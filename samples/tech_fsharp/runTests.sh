#!/bin/bash

#TEST_NAME=test_something

#docker run \
#  -v $(pwd):/sources \
#  -w /sources \
#  -it haskell_dev \
#  ghc -o target/${TEST_NAME} -outputdir target -no-keep-hi-files -no-keep-o-files src/${TEST_NAME}.hs && ./target/${TEST_NAME}

docker run \
  -v $(pwd):/project \
   -w /project/src \
   -it  mcr.microsoft.com/dotnet/sdk \
   dotnet test
