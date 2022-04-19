# Nachos for JAVA (Proj1 ~ Proj2)

## Nachos 소스 컴파일
+ 아래는 Proj1 에서의 Nachos 소스 컴파일을 다룹니다.
    * 1단계. Nachos 프로젝트 폴더의 proj1 디렉토리로 이동
    ![image](https://user-images.githubusercontent.com/33450535/163915972-494c25f9-60b4-4191-88cf-41bf35261194.png)
    * 2단계. proj1 디렉토리에 'nachos' 바이너리가 존재하는지 확인
    ![image](https://user-images.githubusercontent.com/33450535/163916059-01feeda3-b3c8-4966-90ce-370615dfe79c.png)
      - 바이너리 존재 시, 'rm -rf nachos' 명령어를 통해 기존 바이너리 삭제
      ![image](https://user-images.githubusercontent.com/33450535/163916183-b41f21b8-74fc-4bc8-9af9-1f2d1a002f79.png)
    * 3단계. make 명령어로 Nachos 소스 컴파일
    ![image](https://user-images.githubusercontent.com/33450535/163917034-bf3fd7f0-16e9-4287-b04e-80454c3d838a.png)
    * 4단계. 현재 디렉토리 유지한 채, '../bin/nachos' 명령 통해 proj1 에 해당(nachos/threads 디렉토리 파일들)하는 Nachos 가상 머신 작동
    ![image](https://user-images.githubusercontent.com/33450535/163917235-ea276259-91e7-4483-b003-143443a19a0c.png)
+ 아래는 Proj2 에서의 Nachos 소스 컴파일을 다룹니다.
    * 1단계. 해당 링크(https://cseweb.ucsd.edu/classes/fa16/cse120-a/projects/syscall-testing.html) 에서 
    6. 해당되는 도메인 접속하여 재설치 진행 
