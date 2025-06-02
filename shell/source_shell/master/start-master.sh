#!/bin/bash

DB_HOST=$1
DB_USERNAME=$2
DB_PASSWORD=$3
EUREKA_ZONE=$4
SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

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
  version=$(java -version 2>&1 | awk -F '"' '/version/{print $2}')
  if [[ "$version" == "1."* ]]; then
    echo "$version" | awk -F '.' '{print $2}'
  else
    echo "$version" | awk -F '.' '{print $1}'
  fi
}

mkdir -p "$SCRIPT_DIR/master_log"

WINDY_MASTER_JAR=$(find_jar_file "$SCRIPT_DIR" 'windy-master*.jar')
echo "启动 master ..."

JAVA_OPTS="-DDB_HOST=$DB_HOST -DDB_USERNAME=$DB_USERNAME -DDB_PASSWORD=$DB_PASSWORD -DEUREKA_ZONE=$EUREKA_ZONE"
if (( $(get_java_version) >= 9 )); then
  JAVA_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED $JAVA_OPTS"
fi

cd "$SCRIPT_DIR"

# 启动 master 服务
nohup java $JAVA_OPTS -jar "$WINDY_MASTER_JAR" > "$SCRIPT_DIR/master_log/master.log" 2>&1 &
