import SockJS from 'sockjs-client';
import Stomp, { Client } from 'stompjs';

//STOMP (Simple Text Oriented Messaging Protocol) client: sits on top of a WebSocket connection
let stompClient: Client | null = null;

export interface Guess {
  guessId: string;
  userId: string;
  time: number;
  guessX: number;
  guessY: number;
  buildingId: string;
  floor: number;
  roundId: string;
}

export function connect(userId: string, onGuessReceived: (guess: Guess) => void) {
  const socket = new SockJS('http://localhost:5173/ws-game');
  stompClient = Stomp.over(socket);

  stompClient.connect({}, () => {
    console.log('Connected to WebSocket');

    // Subscribe to game updates
    stompClient?.subscribe('/topic/guesses', (message) => {
      const guess: Guess = JSON.parse(message.body);
      onGuessReceived(guess);
    });
  });
}

export function sendGuess(guess: Guess) {
  if (!stompClient) return;
  stompClient.send('/app/guess ', {}, JSON.stringify(guess));
}
