package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

import java.io.EOFException;

//----------------------------------------
import java.util.*;
//----------------------------------------


/**
 * Encapsulates the state of a user process that is not contained in its
 * user thread (or threads). This includes its address translation state, a
 * file table, and information about the program being executed.
 *
 * <p>
 * This class is extended by other classes to support additional functionality
 * (such as additional syscalls).
 *
 * @see	nachos.vm.VMProcess
 * @see	nachos.network.NetProcess
 */

public class UserProcess {
	/**
	 * Allocate a new process.
	 */

	public UserProcess() {
		int numPhysPages = Machine.processor().getNumPhysPages();
		pageTable = new TranslationEntry[numPhysPages];

		for (int i = 0; i < numPhysPages; i++) {
			pageTable[i] = new TranslationEntry(i, 0, false, false, false, false);
		}

		//create a new lock for the function implementation
		lock = new Lock();
		boolean stat = Machine.interrupt().disable();
		lock1.acquire();
		// processID = UserKernel.numProcess++; // LEX please check this
		processID = counter++;
		lock1.release();
		
		// Initialize fileList, filePosList, and fileDeleteList
		fileList = new OpenFile[MAX_FILES];
		filePosList = new int[MAX_FILES];
		fileDeleteList = new HashSet<String>();
		
		// Set fileList's first 2 elements with stdin and stdout (supported by console)
		fileList[STDINPUT] = UserKernel.console.openForReading();
		fileList[STDOUTPUT] = UserKernel.console.openForWriting();
		Machine.interrupt().restore(stat);

		//make the parent process null for a given user process
		parentProcess = null;
		//make a list of child processes for a "parent" to have if it does have children
		childProcesses = new LinkedList<UserProcess>();
		//maintain the list of children status regarding whether to exit or not
		childProcessStatus = new HashMap<Integer, Integer>();
	}

	/**
	 * Allocate and return a new process of the correct class. The class name is
	 * specified by the <tt>nachos.conf</tt> key
	 * <tt>Kernel.processClassName</tt>.
	 * 
	 * @return a new process of the correct class.
	 */
	public static UserProcess newUserProcess() {
		return (UserProcess) Lib.constructObject(Machine.getProcessClassName());
	}

	/**
	 * Execute the specified program with the specified arguments. Attempts to
	 * load the program, and then forks a thread to run it.
	 * 
	 * @param name
	 *            the name of the file containing the executable.
	 * @param args
	 *            the arguments to pass to the executable.
	 * @return <tt>true</tt> if the program was successfully executed.
	 */
	public boolean execute(String name, String[] args) {
		if (!load(name, args)) {
			return false;
		}
		thread = (UThread) (new UThread(this).setName(name));
		thread.fork();
		return true;
	}

	/**
	 * Save the state of this process in preparation for a context switch.
	 * Called by <tt>UThread.saveState()</tt>.
	 */
	public void saveState() {
	}

	/**
	 * Restore the state of this process after a context switch. Called by
	 * <tt>UThread.restoreState()</tt>.
	 */
	public void restoreState() {
		Machine.processor().setPageTable(pageTable);
	}

	/* 아래 함수의 역할(인자, 반환값, 함수의 역할 개요)과 자세한 실행 로직(알고리즘)을 보고서에 적으시오 */
	protected boolean didAllocate(int vpn, int desiredPages, boolean readOnly) {

		LinkedList<TranslationEntry> allocated = new LinkedList<TranslationEntry>();

		for (int i = 0; i < desiredPages; i++) {
			if (vpn >= pageTable.length)
				return false;
			int ppn = UserKernel.getPage();
			if (ppn != -1) {
				TranslationEntry a = new TranslationEntry(vpn + i, ppn, true, readOnly, false, false);
				allocated.add(a);
				pageTable[vpn + i] = a;
				++numPages;
			}
			
			else {
				for (TranslationEntry te : allocated) {
					pageTable[te.vpn] = new TranslationEntry(te.vpn, 0, false, false, false, false);
					UserKernel.deletePage(te.ppn);
					--numPages;
				}
				return false;
			}
		}
		return true;
	}

	/**
     * String readVirtualMemoryString(int vaddr, int maxLength)
     * 지정한 가상 메모리(이 User Process)에서, C-Style 문자열(Null 로 끝나는 문자열)을 읽어옵니다.
     * 읽어온 문자열은 java 의 String 객체로 변환합니다.
	 * 
	 * @param vaddr
	 *            Null 로 끝나는 문자열의 시작 부분에 대한 가상 주소
	 * @param maxLength
	 *            읽어들일 수 있는 문자열의 최대 길이 (끝의 Null 문자 제외)
	 * @return 성공적으로 문자열을 읽어들인 경우 읽어들인 문자열 반환, 실패한 경우 Null
	 */
	public String readVirtualMemoryString(int vaddr, int maxLength) {
		Lib.assertTrue(maxLength >= 0);

		byte[] bytes = new byte[maxLength + 1];                     

		int bytesRead = readVirtualMemory(vaddr, bytes);

		for (int length = 0; length < bytesRead; length++) {
			if (bytes[length] == 0)
				return new String(bytes, 0, length);
		}

		return null;
	}

	/**
	 * int readVirtualMemory(int vaddr, byte[] data)
	 * 지정한 가상 주소의 Byte 에서부터, 특정 길이 만큼의 Byte Data 를 읽어들임
	 * 해당 메소드는 내부적으로, int readVirtualMemory(int vaddr, byte[] data, int offset, int length) 메소드 호출
	 * @param vaddr
	 *            Read 할 첫 번째 Byte에 대한 가상 주소값 
	 * @param data
	 *            Read 한 데이터가 저장될 Buffer
	 * @return    Read 한 데이터의 길이 (Byte 단위)
	 */
	public int readVirtualMemory(int vaddr, byte[] data) {
		return readVirtualMemory(vaddr, data, 0, data.length);  
	}

	/**
	 * int readVirtualMemory(int vaddr, byte[] data, int offset, int length)
	 * 
	 * @param vaddr
	 *            Read 할 첫 번째 Byte에 대한 가상 주소값
	 * @param data
	 *            Read 한 데이터가 저장될 Buffer.
	 * @param offset
	 *            the first byte to write in the array.
	 * @param length
	 *            Read 하고자 하는 데이터의 길이 (Byte 단위)
	 * @return    Read 한 데이터의 길이 (Byte 단위).
	 */
	public int readVirtualMemory(int vaddr, byte[] data, int offset, int length) {
		// Offset 과, Data Length 가 음수가 아니고, 전체 Read 하고자하는 Data의 길이가 Buffer 의 길이를 초과하지 않도록 함
		Lib.assertTrue(offset >= 0 && length >= 0 && offset + length <= data.length);

		
		// 먼저, 이 User Process 에 할당된 Page 가 없다면, Read 작업이 필요가 없어지므로 리턴
		if(numPages == 0) {
			Lib.debug(dbgProcess,  "Read Virtual Memory: Empty pageTable");
			return 0;
		}

		// Nachos 머신의 메인 메모리에 대한 현재 상태를 Array 로 변환 
		byte[] memory = Machine.processor().getMemory();

		int transfer = 0;                               // Read 된 Data 의 길이(Byte 단위)를 0으로 초기화
		int end = vaddr + length - 1;                   // Data 의 끝 Byte에 대한 가상 주솟값 계산
		
		// Data 의 첫번째 Byte 에 대한 가상 주소값이 음수가 아니고, 끝 Byte 에 대한 가상 주솟값이 User Process 에 할당된 Page들의 범위 내에 있는지 확인
		if (vaddr < 0 || end > Machine.processor().makeAddress(numPages - 1, pageSize - 1)) { // (힌트 : 자세한 것은, Processor.java 파일 참고)
			Lib.debug(dbgProcess, "Read Vritual Memory: Invalid Address");
			return 0;
		}

		// 메인 메모리에서 Data Read 시작
		for (int i = Machine.processor().pageFromAddress(vaddr); i <= Machine.processor().pageFromAddress(end); i++) {
			// 현재 Page Number 가 음수이거나, Page Table 에 가용 페이지가 없거나, 해당 Page Table 가 무효한 Page 를 참조하는 경우, Read 작업 종료
			if ((i < 0 || i > pageTable.length) || pageTable == null || !pageTable[i].valid)
				break;
			
			// 현재 참조되고 있는 Byte가 위치한 Page 의 시작 주소 (가상 주소값)
			int startAddress = Machine.processor().makeAddress(i, 0);
			// 현재 참조되고 있는 Byte가 위치한 Page 의 끝 주소 (가상 주소값)
			int endAddress = Machine.processor().makeAddress(i, pageSize - 1);

			int amount = 0;
			int addressOffset;
			
			/* Read 하고자하는 Data 들의 Page 들 상 배치에 따라, Data 의 복사 방법이 달라질 수 있음. 크게 아래 4가지의 경우에 수 존재 */

			if (vaddr > startAddress && end < endAddress) {       // Read 하고자 하는 Data들이 모두 한 페이지 내에 존재하는 경우
				addressOffset = vaddr - startAddress;
				amount = length;
			} 
			
			else if (vaddr <= startAddress && end < endAddress) { // Read 하고자 하는 Data의 시작 Byte가 이전 Page나, 현재 Page 의 첫번째 Byte 인 경우
				addressOffset = 0;
				amount = end - startAddress + 1;
			} 
			
			else if (vaddr > startAddress && end >= endAddress) { // Read 하고자 하는 Data의 끝 Byte가 다음 Page나, 현재 Page 의 끝 Byte 인 경우
				addressOffset = vaddr - startAddress;
				amount = endAddress - vaddr + 1;
			} 
			
			else {                                                // 그외의 경우, 현재 Page 의 모든 Byte 들을 Buffer 로 Read
				addressOffset = 0;
				amount = pageSize;
			}
			
			int paddr = Machine.processor().makeAddress(pageTable[i].ppn, addressOffset); // 실제 물리적 주소(현재 참조되는 Page 에서, Data 복사가 시작되는 주소)를 가져옴
			System.arraycopy(memory, paddr, data, offset + transfer, amount);             // 지금까지의 정보들(Nachos 메인 메모리, 현재 Page 에서 복사 시작 주소, 복사 끝 주소 Buffer, Copy Data 길이)을 토대로, 실제 Data Read 시작

			// Read 된 Byte 들의 수를 갱신
			transfer += amount;
		}

		return transfer;    // 최종 Read 된 데이터의 길이 (Byte 단위) 반환
	}

	/**
	 * int writeVirtualMemory(int vaddr, byte[] data)
	 * 
	 * @param vaddr
	 *            Data 가 Write 될, 가상 메모리 상의 첫번째 Byte 에 대한 가상 주소
	 * @param data
	 *            가상 메모리에 Write 될 Data 들을 저장하고 있는 Buffer
	 * @return    가상 메모리에 Write 된 데이터의 길이 (Byte 단위)
	 */

	public int writeVirtualMemory(int vaddr, byte[] data) {
		return writeVirtualMemory(vaddr, data, 0, data.length);
	}


	/* 아래 함수의 역할(인자, 반환값, 함수의 역할 개요)과 자세한 실행 로직(알고리즘)을 보고서에 적으시오 */
	public int writeVirtualMemory(int vaddr, byte[] data, int offset, int length) {
		Lib.assertTrue(offset >= 0 && length >= 0 && offset + length <= data.length);
		if(numPages >= pageTable.length) {
			Lib.debug(dbgProcess, "Write Virtual Memory: pageTable full");
			return 0;
		}

		byte[] memory = Machine.processor().getMemory();

		int end = vaddr + length - 1;
		int transfer = 0;

		if (vaddr < 0 || end > Machine.processor().makeAddress(numPages - 1, pageSize - 1)) {
			Lib.debug(dbgProcess, "Write Vritual Memory: Invalid Address");
			return 0;
		}

		for (int i = Machine.processor().pageFromAddress(vaddr); i <= Machine.processor().pageFromAddress(end); i++) {
			if ((i < 0 || i > pageTable.length) || pageTable == null || pageTable[i].readOnly || !pageTable[i].valid)
				break;

			int startAddress = Machine.processor().makeAddress(i, 0);
			int endAddress = Machine.processor().makeAddress(i, pageSize - 1);
			int amount = 0;
			int addressOffset;

			if (vaddr > startAddress && end < endAddress) {
				addressOffset = vaddr - startAddress;
				amount = length;
			} else if (vaddr <= startAddress && end < endAddress) {
				addressOffset = 0;
				amount = end - startAddress + 1;
			} else if (vaddr > startAddress && end >= endAddress) {
				addressOffset = vaddr - startAddress;
				amount = endAddress - vaddr + 1;
			} else {
				addressOffset = 0;
				amount = pageSize;
			}

			int paddr = Machine.processor().makeAddress(pageTable[i].ppn, addressOffset);
			System.arraycopy(data, offset + transfer, memory, paddr, amount);
			transfer += amount;
		}
		return transfer;
	}

	/**
	 * Load the executable with the specified name into this process, and
	 * prepare to pass it the specified arguments. Opens the executable, reads
	 * its header information, and copies sections and arguments into this
	 * process's virtual memory.
	 * 
	 * @param name
	 *            the name of the file containing the executable.
	 * @param args
	 *            the arguments to pass to the executable.
	 * @return <tt>true</tt> if the executable was successfully loaded.
	 */
	protected boolean load(String name, String[] args) {
		Lib.debug(dbgProcess, "UserProcess.load(\"" + name + "\")");

		OpenFile executable = ThreadedKernel.fileSystem.open(name, false);
		if (executable == null) {
			Lib.debug(dbgProcess, "\topen failed");
			return false;
		}

		try {
			coff = new Coff(executable);
		} 
		
		catch (EOFException e) {
			executable.close();
			Lib.debug(dbgProcess, "\tcoff load failed");
			return false;
		}

		// make sure the sections are contiguous and start at page 0
		numPages = 0;
		for (int s = 0; s < coff.getNumSections(); s++) {
			CoffSection section = coff.getSection(s);
			if (section.getFirstVPN() != numPages) {
				coff.close();
				Lib.debug(dbgProcess, "\tfragmented executable");
				return false;
			}
			if (!didAllocate(numPages, section.getLength(), section.isReadOnly())) {
				for (int i = 0; i < pageTable.length; ++i)
					if (pageTable[i].valid) {
						UserKernel.deletePage(pageTable[i].ppn);
						pageTable[i] = new TranslationEntry(pageTable[i].vpn, 0, false, false, false, false);
					}
				numPages = 0;
				return false;
			}
		}

		// make sure the argv array will fit in one page
		byte[][] argv = new byte[args.length][];
		int argsSize = 0;
		for (int i = 0; i < args.length; i++) {
			argv[i] = args[i].getBytes();
			// 4 bytes for argv[] pointer; then string plus one for null byte
			argsSize += 4 + argv[i].length + 1;
		}
		if (argsSize > pageSize) {
			coff.close();
			Lib.debug(dbgProcess, "\targuments too long");
			return false;
		}

		// program counter initially points at the program entry point
		initialPC = coff.getEntryPoint();

		// next comes the stack; stack pointer initially points to top of it
		boolean stackAllocation = didAllocate(numPages, stackPages, false);
		if (!stackAllocation) {
			for (int i = 0; i < pageTable.length; ++i)
				if (pageTable[i].valid) {
					UserKernel.deletePage(pageTable[i].ppn);
					pageTable[i] = new TranslationEntry(pageTable[i].vpn, 0, false, false, false, false);
				}
			numPages = 0;
			return false;
		}

		initialSP = numPages * pageSize;

		// and finally reserve 1 page for arguments
		boolean argumentAllocation = didAllocate(numPages, 1, false);
		if (!argumentAllocation) {
			for (int i = 0; i < pageTable.length; ++i)
				if (pageTable[i].valid) {
					UserKernel.deletePage(pageTable[i].ppn);
					pageTable[i] = new TranslationEntry(pageTable[i].vpn, 0, false, false, false, false);
				}
			numPages = 0;
			return false;
		}

		if (!loadSections())
			return false;

		int entryOffset = (numPages - 1) * pageSize;
		int stringOffset = entryOffset + args.length * 4;
		this.argc = args.length;
		this.argv = entryOffset;
		for (int i = 0; i < argv.length; i++) {
			byte[] stringOffsetBytes = Lib.bytesFromInt(stringOffset);
			Lib.assertTrue(writeVirtualMemory(entryOffset, stringOffsetBytes) == 4);
			entryOffset += 4;
			Lib.assertTrue(writeVirtualMemory(stringOffset, argv[i]) == argv[i].length);
			stringOffset += argv[i].length;
			Lib.assertTrue(writeVirtualMemory(stringOffset, new byte[] { 0 }) == 1);
			stringOffset += 1;
		}
		return true;
	}

	/**
	 * Allocates memory for this process, and loads the COFF sections into
	 * memory. If this returns successfully, the process will definitely be run
	 * (this is the last step in process initialization that can fail).
	 * 
	 * @return <tt>true</tt> if the sections were successfully loaded.
	 */
	protected boolean loadSections() {
		if (numPages > Machine.processor().getNumPhysPages()) {
			coff.close();
			Lib.debug(dbgProcess, "\tinsufficient physical memory");
			return false;
		}

		// load sections
		for (int s = 0; s < coff.getNumSections(); s++) {
			CoffSection section = coff.getSection(s);

			Lib.debug(dbgProcess,
					"\tinitializing " + section.getName() + " section (" + section.getLength() + " pages)");

			for (int i = 0; i < section.getLength(); i++) {
				int vpn = section.getFirstVPN() + i;

				TranslationEntry te = pageTable[vpn];
				if (te == null)
					return false;
				section.loadPage(i, te.ppn);
			}
		}

		return true;
	}

	/**
	 * Release any resources allocated by loadSections()
	 */
	protected void unloadSections() {
		int i;
		for (i = 0; i < pageTable.length; ++i)
			if (pageTable[i].valid) {
				UserKernel.deletePage(pageTable[i].ppn);
				pageTable[i] = new TranslationEntry(pageTable[i].vpn, 0, false, false, false, false);
			}
		numPages = 0;
		for (i = 0; i < 16; i++) {
			if (fileList[i] != null) {
				fileList[i].close();
				fileList[i] = null;
			}
		}
		coff.close();

	}

	/**
	 * Initialize the processor's registers in preparation for running the
	 * program loaded into this process. Set the PC register to point at the
	 * start function, set the stack pointer register to point at the top of the
	 * stack, set the A0 and A1 registers to argc and argv, respectively, and
	 * initialize all other registers to 0.
	 */
	public void initRegisters() {
		Processor processor = Machine.processor();

		// by default, everything's 0
		for (int i = 0; i < Processor.numUserRegisters; i++)
			processor.writeRegister(i, 0);

		// initialize PC and SP according
		processor.writeRegister(Processor.regPC, initialPC);
		processor.writeRegister(Processor.regSP, initialSP);

		// initialize the first two argument registers to argc and argv
		processor.writeRegister(Processor.regA0, argc);
		processor.writeRegister(Processor.regA1, argv);
	}

	/**
	 * Handle the halt() system call.
	 */
	private int handleHalt() {
		if (processID != ROOTPROCESS) {
			return -1;
		}
		Machine.halt();

		Lib.assertNotReached("Machine.halt() did not halt machine!");
		return 0;
	}

	private int handleExit(int status) {
        if (parentProcess != null) {
			//acquire the lock for the parent process
			lock.acquire(); // LEX please check this
			parentProcess.childProcessStatus.put(processID, status);
			lock.release(); // LEX please check this
		}
		
		unloadSections();
		
		int childrenNum = childProcesses.size();
		//iterate through children list
		for (int i = 0; i < childrenNum; i++){
			//remove all the children from the childProcessList
			UserProcess child = childProcesses.removeFirst();
			//set the child parent back to null
			child.parentProcess = null;
		}
		//System.out.println("exit" + processID + status);

		//if the process id is the root terminate 
		if (processID == 0) {
			Kernel.kernel.terminate();
		} else{
			//finish the thread and then return 0
			UThread.finish();
		}
		return 0;
	}

    /* 아래 함수의 역할(인자, 반환값, 함수의 역할 개요)과 자세한 실행 로직(알고리즘)을 보고서에 적으시오 */
	private int handleExec(int virtualAddress, int arg1, int arg2) {

		if (virtualAddress < 0 || arg1 < 0 || arg2 < 0) {
			Lib.debug(dbgProcess, "handleExec:Invalid entry");
			return -1;
		}

		String fileName = readVirtualMemoryString(virtualAddress, 256);

		if (fileName == null) {
			Lib.debug(dbgProcess, "handleExec:file name does not exist");
			return -1;
		}

		if (fileName.contains(".coff") == false) {
			Lib.debug(dbgProcess, "handleExec: Incorrect file format, need to end with .coff");
			return -1;
		}

		String[] argsHolder = new String[arg1];

		for (int i = 0; i < arg1; i++) {
			byte[] argBuffer = new byte[4];
			int memReadLen = readVirtualMemory(arg2 + i * 4, argBuffer);

			if (memReadLen != 4){
				Lib.debug(dbgProcess, "handleExec:argument address incorect size");
				return -1;
			}

			int argVirtualAddress = Lib.bytesToInt(argBuffer, 0);

			String arg = readVirtualMemoryString(argVirtualAddress, 256);

			if (arg == null) {
				Lib.debug(dbgProcess, "handleExec:arugment null");
				return -1;
			}

			argsHolder[i] = arg;
		}

		UserProcess childPro = UserProcess.newUserProcess();

		if (childPro.execute(fileName, argsHolder) == false) {
			Lib.debug(dbgProcess, "handleExec:child process failure");
			return -1;
		}

		childPro.parentProcess = this;

		this.childProcesses.add(childPro);

		return childPro.processID;

	}

    /* 아래 함수의 역할(인자, 반환값, 함수의 역할 개요)과 자세한 실행 로직(알고리즘)을 보고서에 적으시오 */
	private int handleJoin(int processID, int statusVAddr) {
		if (processID < 0 || statusVAddr < 0) {
			return -1;
		}
		UserProcess child = null;
		int numberOfChildren = childProcesses.size();

		for (int i = 0; i < numberOfChildren; i++) {
			if (childProcesses.get(i).processID == processID) {
				child = childProcesses.get(i);
				break;
			}
		}

		if (child == null) {
			Lib.debug(dbgProcess, "handleJoin:processID is not the child");
			return -1;
		}		
		child.thread.join();
		child.parentProcess = null;

		childProcesses.remove(child);

		lock.acquire();
		Integer status = childProcessStatus.get(child.processID);
		lock.release();
		if (status == null) {
			Lib.debug(dbgProcess, "handleJoin:Cannot find the exit status of the child");
			return 0;
		} 
		else {
			byte[] buffer = new byte[4];
			buffer = Lib.bytesFromInt(status);
			if (writeVirtualMemory(statusVAddr, buffer) == 4) {
				return 1;
			} 
            else {
				Lib.debug(dbgProcess, "handleJoin:Write status failed");
				return 0;
			}
		}
	}

	/**
	 * Attempt to open the named disk file, creating it if it does not exist,
	 * and return a file descriptor that can be used to access the file.
	 *
	 * Note that create() can only be used to create files on disk; create() will
	 * never return a file descriptor referring to a stream.
	 *
	 * Returns the new file descriptor, or -1 if an error occurred.
	 * 
	 * @param vaddr
	 * @return the new file descriptor, or -1 if an error occurred
	 * 
	 */
	private int handleCreate(int vaddr) {
		// Extract the file name
		String fileName = readVirtualMemoryString(vaddr, MAX_STRLENGTH);
		
		// Return -1 if the file name is invalid or the list is full
		int fileDescriptor = getAvailIndex();
		if(fileName == null || fileDescriptor == -1 || fileDeleteList.contains(fileName)) {
			return -1;
		}

		// Try creating the OpenFile
		OpenFile file = UserKernel.fileSystem.open(fileName, true);
		
		// Return -1 if the file creation failed
		if(file == null) {
			return -1;
		}
		
		// Insert the file in the fileList and return its file descriptor
		fileList[fileDescriptor] = file;
		return fileDescriptor;
	}

	/**
	 * Attempt to open the named file and return a file descriptor.
	 *
	 * Note that open() can only be used to open files on disk; open() will never
	 * return a file descriptor referring to a stream.
	 *
	 * Returns the new file descriptor, or -1 if an error occurred.
	 * 
	 * @param vaddr
	 * @return the new file descriptor, or -1 if an error occurred.
	 * 
	 */
	private int handleOpen(int vaddr) {
		// Extract the file name
		String fileName = readVirtualMemoryString(vaddr, MAX_STRLENGTH);

		// Return -1 if the file name is invalid or the list is full
		int fileDescriptor = getAvailIndex();
		if(fileName == null || fileDescriptor == -1 || fileDeleteList.contains(fileName)) {
			return -1;
		}

		// Try creating the OpenFile
		OpenFile file = UserKernel.fileSystem.open(fileName, false);

		// Return -1 if the file creation failed
		if(file == null) {
			return -1;
		}

		// Insert the file in the fileList and return its file descriptor
		fileList[fileDescriptor] = file;
		return fileDescriptor;
	}
	
	/**
	 * Attempt to read up to size bytes into buffer from the file or stream
	 * referred to by fileDescriptor.
	 *
	 * On success, the number of bytes read is returned. If the file descriptor
	 * refers to a file on disk, the file position is advanced by this number.
	 *
	 * It is not necessarily an error if this number is smaller than the number of
	 * bytes requested. If the file descriptor refers to a file on disk, this
	 * indicates that the end of the file has been reached. If the file descriptor
	 * refers to a stream, this indicates that the fewer bytes are actually
	 * available right now than were requested, but more bytes may become available
	 * in the future. Note that read() never waits for a stream to have more data;
	 * it always returns as much as possible immediately.
	 *
	 * On error, -1 is returned, and the new file position is undefined. This can
	 * happen if fileDescriptor is invalid, if part of the buffer is read-only or
	 * invalid, or if a network stream has been terminated by the remote host and
	 * no more data is available.
	 * 
	 * @param fileDescriptor
	 * @param vaddr
	 * @param size
	 * @return On success, the number of bytes read is returned. On error, -1 is 
	 * returned, and the new file position is undefined.
	 * 
	 */
	private int handleRead(int fileDescriptor, int vaddr, int size) {
		// Return -1 if the input is invalid
		if(size < 0 || (fileDescriptor >= MAX_FILES || fileDescriptor < 0)
				|| fileList[fileDescriptor] == null) {
			return -1;
		}
		
		// Read up to size bytes and save the number of bytes read
		byte[] readBuffer = new byte[size];
		int bytesRead;
		if(fileDescriptor < 2) { 
			bytesRead = fileList[fileDescriptor].read(readBuffer, 0, size); 
		} 
		else {	
			bytesRead = fileList[fileDescriptor].read(filePosList[fileDescriptor], readBuffer, 0, size);
		}	
		
		// Return -1 if failed to read
		if(bytesRead == -1 || bytesRead == 0) {
			return -1;
		}
		
		// Write the buffer into the virtual memory, update file position, and return bytes transferred
		int bytesTransferred = writeVirtualMemory(vaddr, readBuffer, 0, bytesRead);
		if(fileDescriptor >= 2) {
			filePosList[fileDescriptor] += bytesTransferred;	
		}
		return bytesTransferred;
	}
	
	/**
	 * Attempt to write up to count bytes from buffer to the file or stream
	 * referred to by fileDescriptor. write() can return before the bytes are
	 * actually flushed to the file or stream. A write to a stream can block,
	 * however, if kernel queues are temporarily full.
	 *
	 * On success, the number of bytes written is returned (zero indicates nothing
	 * was written), and the file position is advanced by this number. It IS an
	 * error if this number is smaller than the number of bytes requested. For
	 * disk files, this indicates that the disk is full. For streams, this
	 * indicates the stream was terminated by the remote host before all the data
	 * was transferred.
	 *
	 * On error, -1 is returned, and the new file position is undefined. This can
	 * happen if fileDescriptor is invalid, if part of the buffer is invalid, or
	 * if a network stream has already been terminated by the remote host.
	 * 
	 * @param fileDescriptor
	 * @param vaddr
	 * @param size
	 * @return On success, the number of bytes written is returned (zero indicates nothing
	 * was written), and the file position is advanced by this number. On error, -1 is 
	 * returned, and the new file position is undefined.
	 * 
	 */
	private int handleWrite(int fileDescriptor, int vaddr, int size) {
		// Return -1 if the input is invalid
		if(size < 0 || (fileDescriptor >= MAX_FILES || fileDescriptor < 0)
				|| fileList[fileDescriptor] == null) {
			return -1;
		}
		
		// Count number of buffers to write
		byte[] writeBuffer = new byte[size];
		int bytesToWrite = readVirtualMemory(vaddr, writeBuffer, 0, size);
		
		// Write the file, update file position, and return number of bytes written
		int bytesWritten;
		if(fileDescriptor < 2) { 
			bytesWritten =  fileList[fileDescriptor].write(writeBuffer, 0, bytesToWrite);
		}
		else {
			bytesWritten =  fileList[fileDescriptor].write(filePosList[fileDescriptor], writeBuffer, 0, bytesToWrite);
		}
		if(fileDescriptor >= 2) {	
			filePosList[fileDescriptor] += (bytesWritten > 0) ? bytesWritten : 0;
		}
		return (bytesWritten < size && bytesWritten != 0) ? -1 : bytesWritten;

	}
	
	/**
	 * Close a file descriptor, so that it no longer refers to any file or stream
	 * and may be reused.
	 *
	 * If the file descriptor refers to a file, all data written to it by write()
	 * will be flushed to disk before close() returns.
	 * If the file descriptor refers to a stream, all data written to it by write()
	 * will eventually be flushed (unless the stream is terminated remotely), but
	 * not necessarily before close() returns.
	 *
	 * The resources associated with the file descriptor are released. If the
	 * descriptor is the last reference to a disk file which has been removed using
	 * unlink, the file is deleted (this detail is handled by the file system
	 * implementation).
	 *
	 * Returns 0 on success, or -1 if an error occurred.
	 * 
	 * @param fileDescriptor
	 * @return 0 on success, or -1 if an error occurred.
	 * 
	 */
	private int handleClose(int fileDescriptor) {
		// Return -1 if the input is invalid
		if((fileDescriptor >= MAX_FILES || fileDescriptor < 0)
				|| fileList[fileDescriptor] == null) {
			return -1;
		}
		
		// Close and remove the element from the list
		String fileName = fileList[fileDescriptor].getName();
		fileList[fileDescriptor].close();
		fileList[fileDescriptor] = null;
		filePosList[fileDescriptor] = 0;
		
		// Attempt to delete file if this file is unlinked
		if(fileDeleteList.contains(fileName)) {	
			if(UserKernel.fileSystem.remove(fileName) == true) {	
				fileDeleteList.remove(fileName);	
				return 0;	
			}	
			else {
				return -1;	
			}	
		}	
		
		return 0;	// success
	}
	
	/**
	 * Delete a file from the file system. If no processes have the file open, the
	 * file is deleted immediately and the space it was using is made available for
	 * reuse.
	 *
	 * If any processes still have the file open, the file will remain in existence
	 * until the last file descriptor referring to it is closed. However, creat()
	 * and open() will not be able to return new file descriptors for the file
	 * until it is deleted.
	 *
	 * Returns 0 on success, or -1 if an error occurred.
	 * 
	 * @param vaddr
	 * @return 0 on success, or -1 if an error occurred.
	 * 
	 */
	private int handleUnlink(int vaddr) {
		// Extract the file name
		String fileName = readVirtualMemoryString(vaddr, MAX_STRLENGTH);

		// Return -1 if the file name is invalid
		if(fileName == null) {
			return -1;
		}
		
		// Search for index
		
		// Attempt to remove the file from the UserKernel's fileSystem
		boolean removeSuccess = UserKernel.fileSystem.remove(fileName);

		// Just unlink if the file is being used by other processes
		if(removeSuccess == false) {	
			fileDeleteList.add(fileName);	
			return -1;	
		}	
		
		return 0;	// success	
	}
	
	/* System 호출 번호 정의 */
	protected static final int syscallHalt = 0, syscallExit = 1, syscallExec = 2, syscallJoin = 3, syscallCreate = 4,
			syscallOpen = 5, syscallRead = 6, syscallWrite = 7, syscallClose = 8, syscallUnlink = 9;

	/**
     * int handleSyscall(int syscall, int a0, int a1, int a2, int a3)
	 * Syscall 로 인한 인터럽트에 대한 Service Routine 
	 * @param syscall
	 *            호출하고자 하는 Syscall 번호
     *            0 : Halt Syscall
     *            1 : Exit Syscall
     *            2 : Exec Syscall
     *            3 : Join Syscall
     *            4 : Creat Syscall
     *            5 : Open Syscall
     *            6 : Read Syscall
     *            7 : Write Syscall
     *            8 : Close Syscall
     *            9 : Unlink Syscall
	 * @param a0
	 *            해당 Syscall 의 첫번째 인자
	 * @param a1
	 *            해당 Syscall 의 두번째 인자
	 * @param a2
	 *            해당 Syscall 의 세번째 인자
	 * @param a3
	 *            해당 Syscall 의 네번째 인자
	 * @return 해당 Syscall 의 반환값
	 */
	public int handleSyscall(int syscall, int a0, int a1, int a2, int a3) {
		switch (syscall) {
		case syscallHalt:                                                           // Halt 시스템 호출
                        Lib.debug(dbgProcess,"Halt called from process "+processID);
			return handleHalt();                          
		case syscallCreate:                                                         // Create 시스템 호출
                        Lib.debug(dbgProcess,"Create called from process "+processID);
			return handleCreate(a0);
		case syscallOpen:                                                           // Open 시스템 호출
                        Lib.debug(dbgProcess,"Open called from process "+processID);
			return handleOpen(a0);
		case syscallRead:                                                           // Read 시스템 호출
                        Lib.debug(dbgProcess,"Read called from process "+processID);
			return handleRead(a0, a1, a2);
		case syscallWrite:                                                          // Write 시스템 호출
                        Lib.debug(dbgProcess,"Write called from process "+processID);
			return handleWrite(a0, a1, a2);
		case syscallClose:                                                          // Close 시스템 호출
                        Lib.debug(dbgProcess,"Close called from process "+processID);
			return handleClose(a0);
		case syscallUnlink:                                                         // Unlink 시스템 호출
                        Lib.debug(dbgProcess,"Unlink called from process "+processID);
			return handleUnlink(a0);
		case syscallExec:                                                           // Exec 시스템 호출
                        Lib.debug(dbgProcess,"Exec called from process "+processID);
			return handleExec(a0, a1, a2);
		case syscallJoin:                                                           // Join 시스템 호출
                        Lib.debug(dbgProcess,"Join called from process "+processID);
			return handleJoin(a0, a1);
		case syscallExit:                                                           // Exit 시스템 호출
                        Lib.debug(dbgProcess,"Exit called from process "+processID);
			return handleExit(a0);
		default:
			Lib.debug(dbgProcess, "Unknown syscall " + syscall);
			Lib.assertNotReached("Unknown system call!");
		}
		return 0;
	}

	/**
	 * Handle a user exception. Called by <tt>UserKernel.exceptionHandler()</tt>
	 * . The <i>cause</i> argument identifies which exception occurred; see the
	 * <tt>Processor.exceptionZZZ</tt> constants.
	 * 
	 * @param cause
	 *            the user exception that occurred.
	 */
	public void handleException(int cause) {
		Processor processor = Machine.processor();

		switch (cause) {
		case Processor.exceptionSyscall:
			int result = handleSyscall(processor.readRegister(Processor.regV0), processor.readRegister(Processor.regA0),
					processor.readRegister(Processor.regA1), processor.readRegister(Processor.regA2),
					processor.readRegister(Processor.regA3));
			processor.writeRegister(Processor.regV0, result);
			processor.advancePC();
			break;

		default:
			Lib.debug(dbgProcess, "Unexpected exception: " + Processor.exceptionNames[cause]);
			Lib.assertNotReached("Unexpected exception");
		}
	}

	protected OpenFile[] fileList;                         // User Process 에서 사용하는 File 들을 저장하는 Array 정의
	private final int MAX_FILES = 16;                      // 하나의 User Process 는 최대 16 개의 파일을 참조할 수 있음을 정의
	private final int MAX_STRLENGTH = 256;            
	protected int[] filePosList;	                        // corresponding files
	
	/** HashSet of whether the file is to be deleted, not allowing creat or open */
	private static HashSet<String> fileDeleteList;
	
    /**
    protected int getAvailIndex()
    * @param void
    * @return fileList 상에서 가용한 index 반환 (FIFO 방식) / 만일 가용한 index 가 없는 경우 (16 개의 파일들을 이미 참조 중인 경우) -1 반환
     */
	protected int getAvailIndex() {                    
		for(int i = 2; i < MAX_FILES; i++) {     
			if(fileList[i] == null) {
				return i;
			}
		}
		return -1;
	}
	
	
	protected final int STDINPUT = 0;                      // STDIN 스트림에 대한 파일 디스크립터 정의
	protected final int STDOUTPUT = 1;                     // STDOUT 스트림에 대한 파일 디스크립터 정의
	private final int ROOTPROCESS = 0;
	
	public static final int exceptionIllegalSyscall = 100;

	protected Coff coff;                                   // // User Process 에 의해 실행될 .coff 실행 파일 정의

	protected TranslationEntry[] pageTable;                // 이 User Process 에게 할당된 Page 들에 대한 정보 저장할 Page Table 정의
	protected int numPages;                                // 이 User Process 에게 할당된 Page 들의 개수 정의

	protected final int stackPages = Config.getInteger("Processor.numStackPages", 8);   // 이 User Process 의 Stack 으로 할당된 Page 들의 개수(8) 정의

	protected int initialPC, initialSP;                                                 //
	protected int argc, argv;                                                           //

	protected static final int pageSize = Processor.pageSize;                           // Page 하나의 크기를 설정 (힌트 : machine/Processor.java 참고)
	protected static final char dbgProcess = 'a';

    /* Q. 아래 변수가 UserProcess 클래스 내에서의 어떤 역할을 수행하는지, 정의된 목적을 보고서에 설명하세요 */
	protected Lock lock1 = new Lock();

	protected int processID;                                // 이 User Process 의 PID 를 저장할 변수 정의

	//PART 3 VARIABLES
	protected UserProcess parentProcess;                    // Parent Process의 PID 정의
	protected LinkedList<UserProcess> childProcesses;       // Child Process 들을 저장할 List 정의

    /* Q. 아래 변수가 UserProcess 클래스 내에서의 어떤 역할을 수행하는지, 정의된 목적을 보고서에 설명하세요 */
	private static Lock lock; //lock needed for implementation

	protected UThread thread; //thread needed for joining 
	protected HashMap<Integer, Integer> childProcessStatus; // Child Process들과 그 상태를 'PID : Status' 형식으로 저장할 Map 정의
	protected static int counter = 0;                       // 각각의 User Process 에 할당할 PID 를 만들어주기 위한 'PID 계수기 (변수명 : counter)' 정의
}


/**
TranslationEnrty (자세한 것은, machine/TranslationEntry.java 참고)
      : 페이지 테이블의 엔트리를 추상화 시킨 클래스로, Nachos의 페이지 테이블 엔트리는,아래와 같이 구성
      ▶ vpn : Virtual Page Number (페이지 넘버 (가상 메모리의 값))
      ▶ ppn : Physical Page Number (물리적인 페이지 넘버 (실제 프레임의 넘버))
      ▶ valid : 해당 엔트리가 가리키는 페이지가 해당 테이블의 프로세스에게 할당되었는지 혹은, 해당 페이
                 지가 현재 Main Memory 상에 올라와 있는지를 나타냄
      ▶ readOnly : 해당 페이지가 읽기 전용인가를 나타냄 (true : 읽기 전용, false : 읽기/쓰기)
      ▶ used : 해당 엔트리가 가리키는 페이지가 참조된 적이 있는지의 여부를 나타냄
      ▶ dirty : 해당 엔트리가 가리키는 페이지가 수정된(write) 적이 있는지를 나타냄
 */
