#!/bin/bash

docker run \
  -v $(pwd):/project \
   -w /project/src \
   -it  mcr.microsoft.com/dotnet/sdk \
   dotnet test
