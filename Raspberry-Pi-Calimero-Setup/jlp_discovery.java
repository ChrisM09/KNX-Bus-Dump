
/*
* 
* Simple java program to determine where the java library path(s) are on a system.
*
*/
class JLP_Discovery {

	public static void main(String[] args) {

		System.out.println("Java library path = " + System.getProperty("java.library.path"));
	}
}
