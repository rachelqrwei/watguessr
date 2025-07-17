const requireModule = require.context('./modules', false, /\.js$/);
const modules = {};

requireModule.keys().forEach(fileName => {
  const moduleName = fileName.replace(/(\.\/|\.js)/g, '');
  modules[moduleName] = requireModule(fileName).default;
});

export default new Vuex.Store({
  modules
});
