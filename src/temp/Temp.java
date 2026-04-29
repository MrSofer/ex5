/***********/
/* PACKAGE */
/***********/
package temp;

/*******************/
/* GENERAL IMPORTS */
/*******************/

/*******************/
/* PROJECT IMPORTS */
/*******************/

public class Temp
{
	private int serial=0;
	
	public Temp(int serial)
	{
		this.serial = serial;
	}
	
	public int getSerialNumber()
	{
		return serial;
	}

	@Override
	public boolean equals(Object obj) {
		// 1. If they are the exact same object in memory, they are equal
		if (this == obj) return true;

		// 2. If the other object is null or not a Temp, they are not equal
		if (obj == null || getClass() != obj.getClass()) return false;

		// 3. Compare their actual serial numbers!
		Temp other = (Temp) obj;
		return this.serial == other.serial;
	}

	@Override
	public int hashCode() {
		// Generate the hash based entirely on the serial number
		return Integer.hashCode(serial);
	}
}
