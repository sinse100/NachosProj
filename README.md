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


## Proj1 설명
+ ### 0번 과제
  +  Proj1 폴더로 들어가, 컴파일 진행 후, nachos를 실행시키시오.

+ ### 1번 과제
  + ```Alarm``` 클래스의 구현을 마치세요. 하나의 스레드는 ```waitUntil(long x)```를 호출하여, 스스로 실행을 멈출수 있습니다(현재시간 + 인자로 주어진 x). 이 ```waitUntil(long x)``` 메소드와, 타이머 인터럽트 핸들러를 완성시키세요.

+ ### 2번 과제
  + ```KThread.java``` 의 ```Thread Join``` 기법을 위한, ```KThread.join```을 구현하세요. 여기서, ```KTthread.join()``` 함수는 Thread가 종료 될때까지 기다리는 함수입니다. Thread에 대해 ```join()```이 호출되었다면, 다른 ```join()``` 호출은 무시하여도 됩니다. 
```ex) thread 1,2,3이 있을 때, 1에서 2.join()이 호출 된다면 a는 b가 완료 될 때까지 기다린다.```
만약 실행 중인 ```3```이 있었다면 ```3```은 ```2.join()```의 영향을 받지 않고 실행된다.

+ ### 3번 과제
  + 조건 변수들을 구현하세요. 인터럽트 매니저를 다루는 메소드인 ```enable(인터럽트 On!!), disable(인터럽트 Off!!)```를 이용하여, 연산의 원자성을 보장할 수 있습니다. ```Condition``` 클래스는, 세마포어를 활용한 하나의 구현 예시에 불과하며, 저희는, 세마포어를 사용하지 않고, 인터럽트 기능 제어를 통해, 비슷한 기능의 ```Condition2``` 클래스를 만들 것입니다. 


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
  + ```creat, open, read, write, close, unlink``` 의 파일 시스템 호출을 구현하세요. 이 시스템 호출들에 대한 명세 및 의미는 ‘test’ 디렉토리의 ```syscall.h```에 명세 되어있습니다. 또한, ```UserProcess.java``` 에 ```halt``` 시스템 호출이 구현되어있으니 이를 참고하세요. 단, 명심할 것이 여러분들이 파일 시스템을 구현하는 건 아닙니다. 이미 구현되어있는 Nachos 파일 시스템에서, 유저 프로세스가, 파일 시스템에 접근할 수 있도록 시스템 호출을 구현하는 것입니다
  + 일단 ```UserKernel.java``` 파일을 살펴봅시다. 이 ```UserKernel``` 클래스에 대한 설명을 보자면, 다수개의 유저 레벨프로세스를 지원할 수 있는 커널이라고 합니다. 즉, 쉽게 말하면, 멀티프로그래밍이 가능한 Nachos 커널이라는 것을 의미합니다. 일단, Proj1 에서 구현한 ```ThreadedKernel```을 상속하여, ```UserKernel```이 구현된 것을 보아하니, 멀티 쓰레딩을 지원하는 걸로 보입니다.


  + 이번 과제의 목표가 멀티프로그래밍을 지원하는 UserKernel를 구현하는 것인데, 2번 문제를 제외한 나머지 문제들은, 시스템 호출을 구현하는 것이지만, 실제 핵심은 바로, 페이징을 구현하는 것입니다. 정확히 말하자면, 논리적 주소 → 물리적 주소의 변환을 가능케하는 것이라고 볼 수 있습니다. 따라서 어찌보면, 이러한 주소변환을 구현하는 2번 과제(멀티프로그래밍을 가능토록, 페이지 할당을 위한 ```loadSection``` 메소드 및 가상 메모리에서 데이터를 읽어오고, 쓰는 ```readVirtualMemory```, ```writeVirtualMemory``` 메소드의 수정/구
    현)가 먼저 이뤄져야할 것 입니다. 따라서, 이것에 초점을 맞춰 과제를 진행하겠습니다.
    
    
+ ### 2번 과제
  + 원래 Nachos 코드는 UniProgramming을 위해 설계되었습니다. 여러분들의 임무는, 다수의 User Level 프로세스들이 메모리에 한번에 올라올 수 있도록 Multiprogramming을 지원하는 기능을 넣는 것입니다. 여러분들은, 다수의 프로세스들의 주소공간이 겹치지 않도록, 페이지 단위로 각 프로세스에 메모리를 할당할 것입니다. 또한, 페이지 테이블을 추상화시킨 자료구조 ```pageTable```을 각각의 User 프로세스에서 사용하십시오. 이 전체적인 페이징을 통한 MultiProgramming을 가능케하기 위해서는 ```UserProcess``` 클래스의 ```loadSections()``` 메소드, ```readVirtualMemory()``` 메소드, ```writeVirtualMemory``` 메소드를 수정해주세요

## 'Cannot Find Symbol' 오류 발생
  + ㅁㄴㅇㄻㄴㅇㄹ

  
