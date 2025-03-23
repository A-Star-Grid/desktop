tens = 10          # Пример значения
thousands = 7000   # Пример значения
millions = 6_000_000
billions = 9_000_000_000

v1 = (billions * tens) // millions // thousands
v2 = (billions // thousands) * tens // millions
v3 = (billions // millions) * tens // thousands
v4 = (billions // thousands // millions) * tens
v5 = (billions * tens) // thousands // millions

# print(f"v1 = {v1}, v2 = {v2}, v3 = {v3}, v4 = {v4}, v5 = {v5}")
print(int(0.98))