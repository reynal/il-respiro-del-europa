% test FFT pour discrimination harmonic vs souffle
fe=16000;
buflen=8192; % cf BUF_LEN_SAMPLES dans Microphone.java
iter=10; % cf line 65 dans Microphone.java

fid = fopen('wave.bin','r');
X=fread(fid, iter*buflen, 'double', 'ieee-be'); % bit8 ou bit16, cf AudioHub.SAMPLE_SIZE_BYTE
fclose(fid);

%fid = fopen('fft.bin','r');
%Y=fread(fid, iter*buflen, 'double', 'ieee-be'); 
%fclose(fid);

%t=linspace(0,buflen/fe,buflen);
%f=linspace(0,0.5*fe,buflen/2);
%subplot(2,1,1);
%plot(t,X);
%subplot(2,1,2);
%ab=1:80;
%plot(f(ab),Y(ab),'-*')
%plot(f,Y,'-*')

