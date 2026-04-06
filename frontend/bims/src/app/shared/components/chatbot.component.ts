import { Component, inject, signal, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatbotService } from '../../core/services/chatbot.service';

interface ChatMessage {
  sender: 'user' | 'bot';
  text: string;
}

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chatbot.component.html',
  styleUrl: './chatbot.component.css'
})
export class ChatbotComponent implements AfterViewChecked {
  private chatbotService = inject(ChatbotService);

  isOpen = signal(false);
  messages = signal<ChatMessage[]>([]);
  isLoading = signal(false);
  userInput = '';

  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;

  toggleChat() {
    this.isOpen.set(!this.isOpen());
  }

  sendMessage() {
    if (!this.userInput.trim() || this.isLoading()) return;

    const query = this.userInput.trim();
    this.messages.update(m => [...m, { sender: 'user', text: query }]);
    this.userInput = '';
    this.isLoading.set(true);

    this.chatbotService.askQuestion(query).subscribe({
      next: (res) => {
        this.messages.update(m => [...m, { sender: 'bot', text: res.reply }]);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Chat error:', err);
        this.messages.update(m => [...m, { 
          sender: 'bot', 
          text: 'Sorry, I am having trouble connecting right now. Please try again later.' 
        }]);
        this.isLoading.set(false);
      }
    });
  }

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  private scrollToBottom(): void {
    try {
      if (this.messagesContainer) {
        this.messagesContainer.nativeElement.scrollTop = this.messagesContainer.nativeElement.scrollHeight;
      }
    } catch (err) {}
  }
}
