##assets/writings

Contains coded information of 6744 kanji writings taken from KanjiVG
(https://kanjivg.tagaini.net/). The only default versions of kanji writings were taken
(there are also versions like Kaisho or Insatsu for some kanji).

KanjiVG is copyright © 2009-2018 Ulrich Apel and released under the Creative
Commons Attribution-Share Alike 3.0 license.

The file consists of a sequence of writings with no header and with no additional
separators between the writings.

Each writing is saved in the following way:
* 4 bytes for a Unicode integer code point indicating which kanji is it
* 4 bytes for an integer value indicating the number of bytes taken by the rest of
  this writing (this part is skipped, but it can be used to quickly skip writings that
  are not needed to be read)
* 4 bytes for an integer value indicating the number of strokes in this kanji
* strokes of this kanji

Strokes are sequences of cubic or quadratic Bezier curves. Each stroke is saved in the
following way:
* 4 bytes for an integer value indicating the number of curves in this stroke
* curves of this stroke

Each curve is saved in the following way:
* 1 byte indicating the type of this curve
* 32 or 24 bytes for curve coordinates (32 bytes if it's cubic, 24 if it's quadratic)

Type of the curve can be cubic - byte 0x63 (ASCII letter 'c') or quadratic - byte 0x71
(ASCII letter 'q').

Curve coordinates are a sequence of 8 (for cubic) or 6 (for quadratic) float values, where 2
floats (X and Y) in a row are a single point.

In cubic curves, 4 points (8 float values) are:
* start point
* 1 control point
* 2 control point
* endpoint

In quadratic curves, 3 points (6 float values) are:
* start point
* control point
* endpoint


All the coordinates in the file are relative to [0; 0] and represent the curves from
KanjiVG files in the same way with view box bounds [0; 0; 109; 109]. If a kanji should
take the area of 500px square, its coordinates need to be scaled 500/109 (≈4.59x).