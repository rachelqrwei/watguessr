export function stringToHue(input: string): number {
  let hash = 0;
  for (let i = 0; i < input.length; i++) {
    hash = (hash << 5) - hash + input.charCodeAt(i);
    hash |= 0;
  }
  const hue = Math.abs(hash) % 360;
  return hue;
}

export function hslFromHue(hue: number, saturation: number = 100, lightness: number = 75): string {
  return `hsl(${hue}, ${saturation}%, ${lightness}%)`;
}

export function hslaFromHue(
  hue: number,
  saturation: number = 100,
  lightness: number = 75,
  alpha: number = 0.25
): string {
  return `hsla(${hue}, ${saturation}%, ${lightness}%, ${alpha})`;
}

export function colorFromString(input: string, saturation: number = 100, lightness: number = 75): string {
  const hue = stringToHue(input);
  return hslFromHue(hue, saturation, lightness);
}

export function colorPairFromName(
  name: string,
  options?: {
    bgSaturation?: number;
    bgLightness?: number;
    fgSaturation?: number;
    fgLightness?: number;
    fgHueShift?: number;
    guestBg?: string;
    guestFg?: string;
  }
): { bg: string; fg: string } {
  const {
    bgSaturation = 90,
    bgLightness = 75,
    fgSaturation = 100,
    fgLightness = 35,
    fgHueShift = 12,
    guestBg = 'rgba(255, 255, 255, 0.1)',
    guestFg = 'rgba(255, 255, 255, 0.85)',
  } = options || {};

  if (!name || name.toLowerCase() === 'guest') {
    return { bg: guestBg, fg: guestFg };
  }

  const hue = stringToHue(name);
  const shiftedHue = (hue + (fgHueShift % 360) + 360) % 360;
  return {
    bg: hslFromHue(hue, bgSaturation, bgLightness),
    fg: hslFromHue(shiftedHue, fgSaturation, fgLightness),
  };
}


