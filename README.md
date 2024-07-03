
# 리눅스 업데이트 JAR 

여러 페쇄망 서버에서 패키징 이슈로 업데이트 해야할 일이 생겨 매우 러프한 코드로 작성.<br/>

예외처리 및 수정해야할 부분 多

ex) Rocky linux 9.2 -> 9.4 업그레이드 필요시 9.2서버에서 9.4 이미지 파일 준비 후 사용

<br/>

## A. 기본사용법

```shell
./gradlew build
**(PATH)**/build/libs/LinuxOfflineUpdater-1.0.jar

# config 미입력시 hardconding 설정 사용
java -jar linux_upd-1.0.jar **(PATH)**/config.txt
```

## B. config.txt (예시 및 hardconding 내부)

```txt
# 버전을 올릴 iso 현재 위치
ISO_PATH=/tmp/Rocky-9.4-x86_64-dvd.iso

# 마운트할 경로
MOUNT_PATH=/mnt/cdrom

# 수정될 사용 repo경로
REPO_FILE_PATH=/etc/yum.repos.d/rocky.repo
```
