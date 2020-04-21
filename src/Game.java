import java.util.Random;

public class Game {

	char[] classicLetterJamLetters; //64
	char[] playerOneLetters;
	char[] playerTwoLetters;
	char[] playerThreeLetters;
	char[] playerFourLetters;
	char[] playerFiveLetters;
	char[] playerSixLetters;
	char[] availableLetters;
	int dummyPlayers;

	String letters = "AAAABBCCCDDDEEEEEEFFGGHHHIIIIKKLLLMMNNOOOOPPRRRRSSSSTTTTUUUWWYY";

	Game() {
		classicLetterJamLetters = new char[64];
		availableLetters = new char[7];
		playerOneLetters = new char[7];
		playerTwoLetters = new char[7];
		playerThreeLetters = new char[7];
		playerFourLetters = new char[7];
		playerFiveLetters = new char[7];
		playerSixLetters = new char[7];
		processLetters(letters);
		//TODO: add code for number of players
		dummyPlayers = 3;
		shuffle(classicLetterJamLetters);
	}

	private void processLetters(String lettersToBeUsed) {
		for (int i = 0; i < lettersToBeUsed.length(); i++)
		{
			classicLetterJamLetters[i] = lettersToBeUsed.charAt(i);
		}
	}

	private char[] shuffle(char[] array){
		Random rand = new Random();  // Random number generator

		for (int i = 0; i < array.length; i++){
			int randomIndexToSwap = rand.nextInt(array.length);
			char temp = array[randomIndexToSwap];
			array[randomIndexToSwap] = array[i];
			array[i] = temp;
		}

		return array;
	}

	private void replaceAvailableLetters(int[] numbers) {
		//check which letters need replacing
		for (int i = 0; i < numbers.length; i++)
		{
			switch (numbers[i])
			{
				case 1: {
					//replace playerOne letter
				}
				case 2: {}
				case 3: {}
				case 4: {}
				case 5: {}
				case 6: {}
			}
		}
	}

}
