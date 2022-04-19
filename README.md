# Nachos for JAVA (Proj1 ~ Proj2)

## Nachos 소스 컴파일
+ ### 아래는 Proj1 에서의 Nachos 소스 컴파일을 다룹니다.
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
    
- - -
+ ### 아래는 Proj2 에서의 Nachos 소스 컴파일을 다룹니다.
    * 1단계. 해당 링크(https://cseweb.ucsd.edu/classes/fa16/cse120-a/projects/syscall-testing.html) 에서 Nachos 가상 머신 상에서 작동시킬 소스 다운로드
      + write1.c, write4.c, write10.c, snake.c ...
    * 2단계. MIPS 크로스 컴파일러 설치. 해당 링크(https://blog.naver.com/sinse100/222701019568) 참고
    * 3단계. 다운로드한 C 소스 파일들을 nachos/test 디렉토리로 이동
    ![image](https://user-images.githubusercontent.com/33450535/163918129-54bc0dd4-c432-42f9-bcc1-5552b80aa9fa.png)
    ![image](https://user-images.githubusercontent.com/33450535/163918263-3ba6bd8c-2acc-45f2-94c3-ae08b465a4dd.png)
    * 4단계. nachos/test 디렉토리로 이동하여, Makefile 의 TARGET 항목을 아래와 같이 수정. 컴파일하고자 하는 타겟 파일에 우리가 방금 위치시킨 C 소스파일들 이름을 명시
    ![image](https://user-images.githubusercontent.com/33450535/163918451-c8974358-7cba-47bf-8ef8-0e5f29c4ba53.png)
    * 5단계. 이후, make 명령어를 통해 test 디렉토리의 C 소스코드를 coff 확장자의 파일로 컴파일
    ![image](https://user-images.githubusercontent.com/33450535/163919414-442b2302-1b84-4e8f-9408-df344695dcdf.png)
      - 만일, test 디렉토리의 C 소스들을 재 컴파일하고 싶다면, ```make clean``` 명령어 입력
      ![image](https://user-images.githubusercontent.com/33450535/163919538-471f924e-f596-46e2-9dab-a49c4c18ae18.png)
    * 5단계. nachos/proj2 디렉토리로 이동하여, 소스 컴파일 (이하, Proj1 에서의 컴파일과 동일함.)
    ![image](https://user-images.githubusercontent.com/33450535/163918905-1f23f02b-fc5d-4b39-b759-cfb6c69e55a7.png)


    
    6. 해당되는 도메인 접속하여 재설치 진행 
