# GLaDOS

```bash
docker build -t glados .

docker run -it --rm -p 3000:3000 -v ./sqlite:/app/sqlite/ --name glados-container glados
```
