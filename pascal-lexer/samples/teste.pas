program exemplo;
{ Este comentario usa chaves e o analisador deve descarta-lo por inteiro }
var
  x, y : integer;
  total : integer;

function dobro(n : integer) : integer;
begin
  dobro := n * 2
end;

procedure mostrar(a, b : integer);
begin
  writeln('valores lidos: ');
  write(a, b)
end;

begin
  read(x, y);
  total := 0;
  for x := 1 to 10 do
  begin
    total := total + x - 1
  end;
  while (total > 100) do
  begin
    total := total / 2
  end;
  repeat
    total := total + 1
  until (total >= 200);
  (* Este comentario usa parenteses com asterisco e tambem e descartado *)
  if (x < y) then
  begin
    write(x)
  end;
  if (x = y) and not (x <> y) or false then
  begin
    writeln('sao iguais')
  end
  else
  begin
    writeln('sao diferentes')
  end;
  if (x <= y) then
  begin
    mostrar(x, dobro(y))
  end;
  if true then
  begin
    write(total)
  end
end.
