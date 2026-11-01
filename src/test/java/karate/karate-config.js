function fn() {
  var config = {
    baseUrl: karate.properties['baseUrl'] || 'http://localhost:8082'
  };
  karate.configure('connectTimeout', 5000);
  karate.configure('readTimeout', 10000);
  return config;
}
