#!/bin/bash

# --server runs sbt in the foreground (instead of the sbt 2 client & background server), so that the current
# environment variables are used, and the backend is stopped when sbt exits
sbt --server "~backend/reStart"
