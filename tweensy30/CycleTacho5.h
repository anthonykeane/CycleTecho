//**************************************************************************************************************************
//Speedo Code
//**************************************************************************************************************************

//http://www.digitalmisery.com/2013/06/timer-input-capture-on-teensy-3-0/comment-page-1/

const int ledPin = 13;
const int hallPin = 22; // 10k to +5v as pullup ( cannot do software pullup cos in interupt setup) HALL between 22 and GND
uint16_t FTM0Count = 0;
uint16_t FTM0Count1 = 0;
uint16_t FTM0Count2 = 0;
uint16_t FTM0Count3 = 0;
uint32_t WheelRotations = 0;

boolean timerOverflow = true;
int doProcess = false;

// Setup function for FlexTimer0
void setupFTM0() {
  FTM0_FILTER = 0x07;
  FTM0_MODE = 0x05;
  FTM0_SC = 0x00;         // Set this to zero before changing the modulus
  FTM0_CNT = 0x0000;      // Reset the count to zero
  FTM0_MOD = 0xFFFF;      // max modulus = 65535
  FTM0_SC = 0x11;         // TOF=0 TOIE=0 CPWMS=0 CLKS=10 (FF clock) PS=001 (divide by 2)
 
  PORTC_PCR1 |= 0x400; //Teensy pin 22 
  PORTC_PCR2 |= 0x400; //Teensy pin 23 
//  PORTC_PCR3 |= 0x400; //Teensy pin 9
//  PORTC_PCR4 |= 0x400; //Teensy pin 10
  
  FTM0_C0SC = 0x48;       // CHF=0 CHIE=1 (enable interrupt) MSB=0 MSA=0 ELSB=1 (input capture) ELSA=0 DMA=0
  FTM0_C1SC = 0x48;       // CHF=0 CHIE=1 (enable interrupt) MSB=0 MSA=0 ELSB=1 (input capture) ELSA=0 DMA=0
//  FTM0_C2SC = 0x48;       // CHF=0 CHIE=1 (enable interrupt) MSB=0 MSA=0 ELSB=1 (input capture) ELSA=0 DMA=0
//  FTM0_C3SC = 0x48;       // CHF=0 CHIE=1 (enable interrupt) MSB=0 MSA=0 ELSB=1 (input capture) ELSA=0 DMA=0
  NVIC_ENABLE_IRQ(IRQ_FTM0);
}

extern "C" void ftm0_isr(void) {
  digitalWrite(ledPin, !digitalRead(ledPin));
  doProcess = FTM0_STATUS;
  FTM0_C0SC &= ~0x80;
  FTM0_C1SC &= ~0x80;
//  FTM0_C2SC &= ~0x80;
//  FTM0_C3SC &= ~0x80;
  FTM0_CNT = 0x0000;
  FTM0Count = FTM0_C0V;
  FTM0Count1 = FTM0_C1V;
//  FTM0Count2 = FTM0_C2V;
//  FTM0Count3 = FTM0_C3V;
  WheelRotations++;                                     // debounce needed for HALL transistor?
  if ((FTM0_SC&FTM_SC_TOF) != 0) {
    timerOverflow = true;
    FTM0_SC &= ~FTM_SC_TOF;
  }
  else
    timerOverflow = false;
}

//************************************************* Speedo Code*************************************************************
//*
