# Nachos for JAVA (Proj1 ~ Proj2)

## Nachos 소스 컴파일
+ ### 아래는 Proj1 에서의 Nachos 소스 컴파일을 다룹니다.


    * 1단계. Nachos 프로젝트 폴더의 proj1 디렉토리로 이동
    ![image](https://user-images.githubusercontent.com/33450535/163915972-494c25f9-60b4-4191-88cf-41bf35261194.png)
    
    
    * 2단계. proj1 디렉토리에 ```nachos``` 바이너리가 존재하는지 확인
    ![image](https://user-images.githubusercontent.com/33450535/163916059-01feeda3-b3c8-4966-90ce-370615dfe79c.png)
      - 바이너리 존재 시, ```rm -rf nachos``` 명령어를 통해 기존 바이너리 삭제
      ![image](https://user-images.githubusercontent.com/33450535/163916183-b41f21b8-74fc-4bc8-9af9-1f2d1a002f79.png)
      
      
    * 3단계. ```make``` 명령어로 Nachos 소스 컴파일
    ![image](https://user-images.githubusercontent.com/33450535/163917034-bf3fd7f0-16e9-4287-b04e-80454c3d838a.png)
    
    
    * 4단계. 현재 디렉토리 유지한 채, ```../bin/nachos``` 명령 통해 proj1 에 해당(nachos/threads 디렉토리 파일들)하는 Nachos 가상 머신 작동
    ![image](https://user-images.githubusercontent.com/33450535/163917235-ea276259-91e7-4483-b003-143443a19a0c.png)
    

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
      
      
    * 6단계. nachos/proj2 디렉토리로 이동하여, 소스 컴파일 (이하, Proj1 에서의 컴파일과 동일함.)
    ![image](https://user-images.githubusercontent.com/33450535/163918905-1f23f02b-fc5d-4b39-b759-cfb6c69e55a7.png)


- - -

## Proj2 설명
+ ### 배경 지식
  + 수강생 여러분들이 수정할 프로젝트 코드는 ```userprog``` 디렉토리의 다음 두 파일입니다
    - ```UserKernel.java``` : Multiprogramming을 지원하는 Kernel
    - ```UserProcess.java``` : 유저 레벨 프로세스(주소공간)를 관리하며, 유저 프로그램을 가상 메모리로 로드   
  + 위의 두 파일 뿐만 아니라, 아래의 파일들도 꼭 읽어보시고 감을 잡으세요.
    - ```userprog``` 디렉토리의 ```UThread.java, SynchConsole.java```
    - ```machine``` 디렉토리의 ```Processor, FileSystem```
  + ```test``` 디렉토리는 Nachos 가상머신에서 실행가능한 MIPS 프로그램들을 저장하고 있습니다.   


+ ### 1번 과제
  + ```creat, open, read, write, close, unlink``` 의 파일 시스템 호출을 구현하시오. 이 시스템 호출들에 대한 명세 및 의미는 ‘test’ 디렉토리의 syscall.h에 명세 되어있습니다. 또한, ```UserProcess.java``` 에 halt 시스템 호출이 구현되어있으니 이를 참고하세요. 단, 명심할 것이 여러분들이 파일 시스템을 구현하는 건 아닙니다. 이미 구현되어있는 Nachos 파일 시스템에서, 유저 프로세스가, 파일 시스템에 접근할 수 있도록 시스템 호출을 구현하는 것입니다
  + 일단 UserKernel.java 파일을 살펴봅시다. 이 UserKernel 클래스에 대한 설명을 보자면, 다수개의 유저 레벨프로세스를 지원할 수 있는 커널이라고 합니다. 즉, 쉽게 말하면, 멀티프로그래밍이 가능한 Nachos 커널이라는 것을 의미합니다. 일단, Proj1에서 구현한 ThreadedKernel을 상속하여, UserKernel이 구현된 것을 보아하니, 멀티 쓰레딩을 지원하는 걸로 보입니다.
    ![noname01](https://user-images.githubusercontent.com/33450535/163922336-5f0413bf-f535-4397-b891-621067db25fc.jpg)

  
