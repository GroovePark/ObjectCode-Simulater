package sp18_simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;



/**
 * ResourceManager�� ��ǻ���� ���� ���ҽ����� �����ϰ� �����ϴ� Ŭ�����̴�.
 * ũ�� �װ����� ���� �ڿ� ������ �����ϰ�, �̸� ������ �� �ִ� �Լ����� �����Ѵ�.<br><br>
 * 
 * 1) ������� ���� �ܺ� ��ġ �Ǵ� device<br>
 * 2) ���α׷� �ε� �� ������ ���� �޸� ����. ���⼭�� 64KB�� �ִ밪���� ��´�.<br>
 * 3) ������ �����ϴµ� ����ϴ� �������� ����.<br>
 * 4) SYMTAB �� simulator�� ���� �������� ���Ǵ� �����͵��� ���� ������. 
 * <br><br>
 * 2���� simulator������ ����Ǵ� ���α׷��� ���� �޸𸮰����� �ݸ�,
 * 4���� simulator�� ������ ���� �޸� �����̶�� ������ ���̰� �ִ�.
 */
public class ResourceManager{
	/**
	 * deviceManager��  ����̽��� �̸��� �Է¹޾��� �� �ش� ����̽��� ���� ����� ���� Ŭ������ �����ϴ� ������ �Ѵ�.
	 * ���� ���, 'A1'�̶�� ����̽����� ������ read���� ������ ���, hashMap�� <"A1", scanner(A1)> ���� �������μ� �̸� ������ �� �ִ�.
	 * <br><br>
	 * ������ ���·� ����ϴ� �� ���� ����Ѵ�.<br>
	 * ���� ��� key������ String��� Integer�� ����� �� �ִ�.
	 * ���� ������� ���� ����ϴ� stream ���� �������� ����, �����Ѵ�.
	 * <br><br>
	 * �̰͵� �����ϸ� �˾Ƽ� �����ؼ� ����ص� �������ϴ�.
	 */
	HashMap<String,Object> deviceManager = new HashMap<String,Object>();
	char[] memory = new char[65536]; // String���� �����ؼ� ����Ͽ��� ������.
	int[] register = new int[10];
	double register_F;
	int program_length; // ���α׷��� ��ü ���̸� ����
	SymbolTable symtabList = new SymbolTable();
	ArrayList<Integer> sectorlenth; // �� ������ ���̸� ����
	String programname; // ���α׷��� �̸�(COPY)
	String startaddress; // ���α׷��� �����ּ�
	int nowloc = 0; // ��ɾ �����ؾ� �ϴ� �޸��� ���� �ּ�
	
	/**
	 * �޸�, �������͵� ���� ���ҽ����� �ʱ�ȭ�Ѵ�.
	 */
	public void initializeResource(){
		int i;
		// �޸𸮸� ����ִ� ������ x�� (simulator �� 78787878~�� ǥ��, �������͵��� 0���� �ʱ�ȭ)
		for(i=0;i<65536;i++)
			memory[i] = 'x';
		for(i=0;i<10;i++)
			register[i] = 0;
	}
	
	/**
	 * deviceManager�� �����ϰ� �ִ� ���� ����� stream���� ���� �����Ű�� ����.
	 * ���α׷��� �����ϰų� ������ ���� �� ȣ���Ѵ�.
	 * @throws IOException 
	 */
	public void closeDevice() throws IOException {
		//deviceManage �ȿ� �ִ� ��Ʈ���� close ���ش�
		FileReader a = (FileReader) deviceManager.get("F1.txt");
		FileWriter b = (FileWriter) deviceManager.get("05.txt");
		a.close();
		b.close();
	}
	
	/**
	 * ����̽��� ����� �� �ִ� ��Ȳ���� üũ. TD��ɾ ������� �� ȣ��Ǵ� �Լ�.
	 * ����� stream�� ���� deviceManager�� ���� ������Ų��.
	 * @param devName Ȯ���ϰ��� �ϴ� ����̽��� ��ȣ,�Ǵ� �̸�
	 * @throws IOException 
	 */
	public void testDevice(String devName) throws IOException {
		//���� deviceManager�� devName�̶�� �̸��� ���� ��Ʈ���� ������ 
		//�غ� �Ǿ��ִٴ� ���̹Ƿ� Sw�������Ϳ� �غ�Ǿ��ٴ� ���� 1�־��ش�.
		if(deviceManager.containsKey(devName) == true)
			setRegister(9, 1);
		else 
		{	
			try {
				// �� ������ �б�,���� ��� ��Ʈ������ �����ش�.
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
				// ������ ���� ��(read�� �ش�, write�� ����) �غ���� �ʾҴٴ� ������
				// Sw�������Ϳ� 0�� ����
				setRegister(9, 0);
				e.printStackTrace();
			}
		}
	}

	/**
	 * ����̽��κ��� ���ϴ� ������ŭ�� ���ڸ� �о���δ�. RD��ɾ ������� �� ȣ��Ǵ� �Լ�.
	 * @param devName ����̽��� �̸�
	 * @param num �������� ������ ����
	 * @return ������ ������
	 * @throws IOException 
	 */
	public char readDevice(String devName) throws IOException{
		int c;
		FileReader a = (FileReader) deviceManager.get(devName);
		c = a.read();
		return (char) c;
		
	}

	/**
	 * ����̽��� ���ϴ� ���� ��ŭ�� ���ڸ� ����Ѵ�. WD��ɾ ������� �� ȣ��Ǵ� �Լ�.
	 * @param devName ����̽��� �̸�
	 * @param data ������ ������
	 * @param num ������ ������ ����
	 * @throws IOException 
	 */
	public void writeDevice(String devName, char data) throws IOException{
		FileWriter b = (FileWriter) deviceManager.get(devName);
		b.write(String.valueOf(data));
		b.flush();
	}	
	
	/**
	 * �޸��� Ư�� ��ġ���� ���ϴ� ������ŭ�� ���ڸ� �����´�.
	 * @param location �޸� ���� ��ġ �ε���
	 * @param num ������ ����
	 * @return �������� ������
	 */
	public char[] getMemory(int location, int num){
		// char �迭 value�� ����, �ű⿡ ���� �����ѵ� ����
		char[] value = new char[3];
		int i;
		for(i=0;i<num;i++)
		{
			value[i] = memory[location+i];
		}
		
		return value;
		
	}

	/**
	 * �޸��� Ư�� ��ġ�� ���ϴ� ������ŭ�� �����͸� �����Ѵ�. 
	 * @param locate ���� ��ġ �ε���
	 * @param data �����Ϸ��� ������
	 * @param num �����ϴ� �������� ����
	 */
	public void setMemory(int locate, char[] data, int num){
		int i;
		for(i=0; i<num; i++)
			memory[locate] = data[i];
	}

	/**
	 * ��ȣ�� �ش��ϴ� �������Ͱ� ���� ��� �ִ� ���� �����Ѵ�. �������Ͱ� ��� �ִ� ���� ���ڿ��� �ƴԿ� �����Ѵ�.
	 * @param regNum �������� �з���ȣ
	 * @return �������Ͱ� ������ ��
	 */
	public int getRegister(int regNum){
		return register[regNum];
		
	}

	/**
	 * ��ȣ�� �ش��ϴ� �������Ϳ� ���ο� ���� �Է��Ѵ�. �������Ͱ� ��� �ִ� ���� ���ڿ��� �ƴԿ� �����Ѵ�.
	 * @param regNum ���������� �з���ȣ
	 * @param value �������Ϳ� ����ִ� ��
	 */
	public void setRegister(int regNum, int value){
		register[regNum] = value;
	}

	/**
	 * �ַ� �������Ϳ� �޸𸮰��� ������ ��ȯ���� ���ȴ�. int���� char[]���·� �����Ѵ�.
	 * @param data
	 * @return
	 */
	public char[] intToChar(int data){
		String s;
		int i;
		int[] num = new int[3];
		char[] value = new char[3];
		// int ���� 6�ڸ� HEX������ �ٲ� �� 1����Ʈ�� ������ value������ �����Ѵ�.
		// 14�� ���� �����Ѵٰ� �ϸ� int �� 00000E�� �ٲ� �� 00 00 0E�� ������ char �迭�� �����ϴ� ���̴�.
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
	 * �ַ� �������Ϳ� �޸𸮰��� ������ ��ȯ���� ���ȴ�. char[]���� int���·� �����Ѵ�.
	 * @param data
	 * @return
	 */
	public int byteToInt(char[] data){
		int i,value;
		// �޸𸮿� �ִ� ���� ���� �о s�� �̾� ���� �� �װ��� 16���� ��Ʈ�� ��ȯ ��Ų��.
		// 00 00 0E�� ����Ǿ� �ִٰ� �ϸ� 00000E�� �ٲ۵� 16���� ��ȯ�ϸ� 14�� �����Ѵ�.
		String s = "";
		value = 0;
		for(i=0;i<3;i++)
			s +=String.format("%02X", (data[i] | 0));
		value = Integer.parseInt(s,16);
		return value;
	}
	
	/**
	 * getTA �Լ� :
	 * opcode���� format3������ ���� 12��Ʈ, format4�� ���� 20��Ʈ�� ������ �ִ� ���� �����Ѵ�.
	�̰����� IMMEDIATE ���� �ٷ� ���̰� PCRELATIVE �϶��� �ٸ� ������� ���� ������ ���� ����Ű���� �ϴ� �ּҰ��� 
	ã�� ���̴�*/
	public int getTA(int adress,int format)
	{
		String s;
		int num1,num2,num3,num4,num5;
		if(format == 3)
		{	
			//format3�Ͻ� ��Ʈ ������ ���� ���� �����ִ� �����̴�. 
			num1 = memory[adress+1] & 15;
			num2 = memory[adress+2] >> 4;
			num3 = memory[adress+2] & 15;
			//��Ʈ ���� �� ������ 16�������ڿ��� �ٲٰ� �̾� ���� ��
			s = String.format("%01X",num1)+String.format("%01X",num2)+String.format("%01X",num3);
			// �ٽ� int�����κ�ȯ�ؼ� �����Ѵ�.
			return Integer.parseInt(s,16);
		}
		if(format == 4)
		{	//format4 �Ͻ� ��Ʈ������ ���� ���� �����ִ� �����̴�.
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