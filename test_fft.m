clear all
% test FFT pour discrimination harmonic vs souffle
fe=16000;
buflen=4096; % cf BUF_LEN_SAMPLES dans Microphone.java
rf=1:buflen/2;
rt=1:buflen;
iter=20; % cf line 65 dans Microphone.java

fid = fopen('wave.bin.voiced','r');
X=fread(fid, [buflen iter], 'double', 'ieee-be'); % bit8 ou bit16, cf AudioHub.SAMPLE_SIZE_BYTE
fclose(fid);

fid = fopen('fft.bin.voiced','r');
Y=fread(fid, [buflen/2 iter], 'double', 'ieee-be'); 
fclose(fid);

t=linspace(0,buflen/fe,buflen);
f=linspace(0,0.5*fe,buflen/2);
%subplot(2,1,1);
%plot(t,X);
%subplot(2,1,2);
%ab=1:80;
%plot(f(ab),Y(ab),'-*')
%plot(f,Y,'-*')

%fx = fft([xx zeros(1,4096)]);
%x2 = ifft(fx.*conj(fx));
%n =  4096
%x2 = [x2(n+2:end) x2(1:n)];