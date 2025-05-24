#!/bin/bash

MAVEN_PATH=$1
EUREKA_ZONE=$2
SCRIPT_DIR=$(cd -P "$(dirname "$SOURCE")" && pwd)

find_jar_file() {
  local DIR=$1
  local PATTERN=$2
  local JAR_FILE=$(find "$DIR" -name "$PATTERN" | head -n 1)
  if [ -z "$JAR_FILE" ]; then
    echo "未找到可执行的jar, 跳过执行"
    exit 1
  fi
  echo "$JAR_FILE"
}

# 获取JDK主版本号
get_java_version() {
  local version
  version=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}')
  if [[ "$version" == "1."* ]]; then
    # JDK 8及以下版本格式为1.x.x
    echo "$version" | awk -F '.' '{print $2}'
  else
    # JDK 9及以上版本格式为x.x.x
    echo "$version" | awk -F '.' '{print $1}'
  fi
}

mkdir -p "$SCRIPT_DIR/client_log"

WINDY_CLIENT_JAR=$(find_jar_file "$SCRIPT_DIR/client" 'windy-client*.jar')
echo "启动 client..."

JAVA_OPTS="-Dwindy.pipeline.maven.path=\"$MAVEN_PATH\" -DEUREKA_ZONE=\"$EUREKA_ZONE\""
if (( $(get_java_major_version) >= 9 )); then
  JAVA_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED $JAVA_OPTS"
fi

nohup java $JAVA_OPTS -jar "$WINDY_CLIENT_JAR" > "$SCRIPT_DIR/client_log/client.log" 2>&1 &
