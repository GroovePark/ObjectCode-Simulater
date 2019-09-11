package sp18_simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;



/**
 * ResourceManager는 컴퓨터의 가상 리소스들을 선언하고 관리하는 클래스이다.
 * 크게 네가지의 가상 자원 공간을 선언하고, 이를 관리할 수 있는 함수들을 제공한다.<br><br>
 * 
 * 1) 입출력을 위한 외부 장치 또는 device<br>
 * 2) 프로그램 로드 및 실행을 위한 메모리 공간. 여기서는 64KB를 최대값으로 잡는다.<br>
 * 3) 연산을 수행하는데 사용하는 레지스터 공간.<br>
 * 4) SYMTAB 등 simulator의 실행 과정에서 사용되는 데이터들을 위한 변수들. 
 * <br><br>
 * 2번은 simulator위에서 실행되는 프로그램을 위한 메모리공간인 반면,
 * 4번은 simulator의 실행을 위한 메모리 공간이라는 점에서 차이가 있다.
 */
public class ResourceManager{
	/**
	 * deviceManager는  디바이스의 이름을 입력받았을 때 해당 디바이스의 파일 입출력 관리 클래스를 리턴하는 역할을 한다.
	 * 예를 들어, 'A1'이라는 디바이스에서 파일을 read모드로 열었을 경우, hashMap에 <"A1", scanner(A1)> 등을 넣음으로서 이를 관리할 수 있다.
	 * <br><br>
	 * 변형된 형태로 사용하는 것 역시 허용한다.<br>
	 * 예를 들면 key값으로 String대신 Integer를 사용할 수 있다.
	 * 파일 입출력을 위해 사용하는 stream 역시 자유로이 선택, 구현한다.
	 * <br><br>
	 * 이것도 복잡하면 알아서 구현해서 사용해도 괜찮습니다.
	 */
	HashMap<String,Object> deviceManager = new HashMap<String,Object>();
	char[] memory = new char[65536]; // String으로 수정해서 사용하여도 무방함.
	int[] register = new int[10];
	double register_F;
	int program_length; // 프로그램의 전체 길이를 저장
	SymbolTable symtabList = new SymbolTable();
	ArrayList<Integer> sectorlenth; // 각 섹터의 길이를 저장
	String programname; // 프로그램의 이름(COPY)
	String startaddress; // 프로그램의 시작주소
	int nowloc = 0; // 명령어를 수행해야 하는 메모리의 현재 주소
	
	/**
	 * 메모리, 레지스터등 가상 리소스들을 초기화한다.
	 */
	public void initializeResource(){
		int i;
		// 메모리를 비어있는 값으로 x를 (simulator 상에 78787878~로 표시, 레지스터들은 0으로 초기화)
		for(i=0;i<65536;i++)
			memory[i] = 'x';
		for(i=0;i<10;i++)
			register[i] = 0;
	}
	
	/**
	 * deviceManager가 관리하고 있는 파일 입출력 stream들을 전부 종료시키는 역할.
	 * 프로그램을 종료하거나 연결을 끊을 때 호출한다.
	 * @throws IOException 
	 */
	public void closeDevice() throws IOException {
		//deviceManage 안에 있는 스트림을 close 해준다
		FileReader a = (FileReader) deviceManager.get("F1.txt");
		FileWriter b = (FileWriter) deviceManager.get("05.txt");
		a.close();
		b.close();
	}
	
	/**
	 * 디바이스를 사용할 수 있는 상황인지 체크. TD명령어를 사용했을 때 호출되는 함수.
	 * 입출력 stream을 열고 deviceManager를 통해 관리시킨다.
	 * @param devName 확인하고자 하는 디바이스의 번호,또는 이름
	 * @throws IOException 
	 */
	public void testDevice(String devName) throws IOException {
		//먼저 deviceManager에 devName이라는 이름의 파일 스트림이 있으면 
		//준비가 되어있다는 말이므로 Sw레지스터에 준비되었다는 뜻의 1넣어준다.
		if(deviceManager.containsKey(devName) == true)
			setRegister(9, 1);
		else 
		{	
			try {
				// 각 파일을 읽기,쓰기 모드 스트림으로 열어준다.
				if(devName.equals("F1.txt"))
				{
				FileReader filereader = new FileReader(devName);
				deviceManager.put(devName, filereader);
				}
				if(devName.equals("05.txt"))
				{
				FileWriter filewriter = new FileWriter(devName ,true);
				deviceManager.put(devName, filewriter);
				}
				setRegister(9, 1);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				// 파일이 없을 시(read만 해당, write는 생성) 준비되지 않았다는 뜻으로
				// Sw레지스터에 0값 세팅
				setRegister(9, 0);
				e.printStackTrace();
			}
		}
	}

	/**
	 * 디바이스로부터 원하는 개수만큼의 글자를 읽어들인다. RD명령어를 사용했을 때 호출되는 함수.
	 * @param devName 디바이스의 이름
	 * @param num 가져오는 글자의 개수
	 * @return 가져온 데이터
	 * @throws IOException 
	 */
	public char readDevice(String devName) throws IOException{
		int c;
		FileReader a = (FileReader) deviceManager.get(devName);
		c = a.read();
		return (char) c;
		
	}

	/**
	 * 디바이스로 원하는 개수 만큼의 글자를 출력한다. WD명령어를 사용했을 때 호출되는 함수.
	 * @param devName 디바이스의 이름
	 * @param data 보내는 데이터
	 * @param num 보내는 글자의 개수
	 * @throws IOException 
	 */
	public void writeDevice(String devName, char data) throws IOException{
		FileWriter b = (FileWriter) deviceManager.get(devName);
		b.write(String.valueOf(data));
		b.flush();
	}	
	
	/**
	 * 메모리의 특정 위치에서 원하는 개수만큼의 글자를 가져온다.
	 * @param location 메모리 접근 위치 인덱스
	 * @param num 데이터 개수
	 * @return 가져오는 데이터
	 */
	public char[] getMemory(int location, int num){
		// char 배열 value를 선언, 거기에 값을 저장한뒤 리턴
		char[] value = new char[3];
		int i;
		for(i=0;i<num;i++)
		{
			value[i] = memory[location+i];
		}
		
		return value;
		
	}

	/**
	 * 메모리의 특정 위치에 원하는 개수만큼의 데이터를 저장한다. 
	 * @param locate 접근 위치 인덱스
	 * @param data 저장하려는 데이터
	 * @param num 저장하는 데이터의 개수
	 */
	public void setMemory(int locate, char[] data, int num){
		int i;
		for(i=0; i<num; i++)
			memory[locate] = data[i];
	}

	/**
	 * 번호에 해당하는 레지스터가 현재 들고 있는 값을 리턴한다. 레지스터가 들고 있는 값은 문자열이 아님에 주의한다.
	 * @param regNum 레지스터 분류번호
	 * @return 레지스터가 소지한 값
	 */
	public int getRegister(int regNum){
		return register[regNum];
		
	}

	/**
	 * 번호에 해당하는 레지스터에 새로운 값을 입력한다. 레지스터가 들고 있는 값은 문자열이 아님에 주의한다.
	 * @param regNum 레지스터의 분류번호
	 * @param value 레지스터에 집어넣는 값
	 */
	public void setRegister(int regNum, int value){
		register[regNum] = value;
	}

	/**
	 * 주로 레지스터와 메모리간의 데이터 교환에서 사용된다. int값을 char[]형태로 변경한다.
	 * @param data
	 * @return
	 */
	public char[] intToChar(int data){
		String s;
		int i;
		int[] num = new int[3];
		char[] value = new char[3];
		// int 값을 6자리 HEX값으로 바꾼 뒤 1바이트씩 나눠서 value베열에 저장한다.
		// 14란 값을 저장한다고 하면 int 값 00000E로 바꾼 뒤 00 00 0E로 나눠서 char 배열에 저장하는 것이다.
		s = String.format("%06X",data);
		num[0] = Integer.parseInt(s.substring(0,2),16);
		num[1] = Integer.parseInt(s.substring(2,4),16);
		num[2] = Integer.parseInt(s.substring(4,6),16);
		for(i=0;i<3;i++)
		{
			value[i] = (char)(0 | num[i]);
		}
		return value;
	}

	/**
	 * 주로 레지스터와 메모리간의 데이터 교환에서 사용된다. char[]값을 int형태로 변경한다.
	 * @param data
	 * @return
	 */
	public int byteToInt(char[] data){
		int i,value;
		// 메모리에 있는 값을 값을 읽어서 s에 이어 붙인 뒤 그것을 16진수 인트형 변환 시킨다.
		// 00 00 0E가 저장되어 있다고 하면 00000E로 바꾼뒤 16진수 변환하면 14를 리턴한다.
		String s = "";
		value = 0;
		for(i=0;i<3;i++)
			s +=String.format("%02X", (data[i] | 0));
		value = Integer.parseInt(s,16);
		return value;
	}
	
	/**
	 * getTA 함수 :
	 * opcode에서 format3에서는 뒤의 12비트, format4는 뒤의 20비트가 가지고 있는 값을 리턴한다.
	이값들은 IMMEDIATE 때는 바로 쓰이고 PCRELATIVE 일때는 다른 값들과의 덧셈 뺄셈을 통해 가리키고자 하는 주소값을 
	찾을 것이다*/
	public int getTA(int adress,int format)
	{
		String s;
		int num1,num2,num3,num4,num5;
		if(format == 3)
		{	
			//format3일시 비트 연산을 통해 값을 구해주는 과정이다. 
			num1 = memory[adress+1] & 15;
			num2 = memory[adress+2] >> 4;
			num3 = memory[adress+2] & 15;
			//비트 연산 한 값들은 16진수문자열로 바꾸고 이어 붙인 뒤
			s = String.format("%01X",num1)+String.format("%01X",num2)+String.format("%01X",num3);
			// 다시 int형으로변환해서 리턴한다.
			return Integer.parseInt(s,16);
		}
		if(format == 4)
		{	//format4 일시 비트연산을 통해 값을 구해주는 과정이다.
			num1 = memory[adress+1] & 15;
			num2 = memory[adress+2] >> 4;
			num3 = memory[adress+2] & 15;
			num4 = memory[adress+3] >> 4;
			num5 = memory[adress+3] & 15;
			s = String.format("%01X",num1)+String.format("%01X",num2)+String.format("%01X",num3)
				+String.format("%01X",num4)+String.format("%01X",num5);
			
			return Integer.parseInt(s,16);
		}
		return 0;
	}
}