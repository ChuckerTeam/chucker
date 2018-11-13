#!/bin/sh
# Require 3 files:
# - chucker-http.mp4
# - chucker-error.mp4
# - chucker-multiwindow.mp4

ffmpeg -y -i chucker-http.mp4 -vf fps=10,scale=310:-1:flags=lanczos,palettegen palette.png
ffmpeg -i chucker-http.mp4 -i palette.png -filter_complex "fps=10,scale=310:-1:flags=lanczos[x];[x][1:v]paletteuse" chucker-http.gif

ffmpeg -y -i chucker-error.mp4 -vf fps=10,scale=310:-1:flags=lanczos,palettegen palette.png
ffmpeg -i chucker-error.mp4 -i palette.png -filter_complex "fps=10,scale=310:-1:flags=lanczos[x];[x][1:v]paletteuse" chucker-error.gif

ffmpeg -y -i chucker-multiwindow.mp4 -vf fps=10,scale=720:-1:flags=lanczos,palettegen palette.png
ffmpeg -i chucker-multiwindow.mp4 -i palette.png -filter_complex "fps=10,scale=720:-1:flags=lanczos[x];[x][1:v]paletteuse" chucker-multiwindow.gif
