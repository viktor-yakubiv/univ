/* PIT Theme Gulpfile
 * Copyright © 2017 Viktor Yakubiv
 */

const gulp          = require('gulp');
const del           = require('del');
const filter        = require('gulp-filter');
const plumber       = require('gulp-plumber');
const rename        = require('gulp-rename');
const runSequence   = require('run-sequence');

const postcss       = require('gulp-postcss');
const autoprefixer  = require('autoprefixer');
const cssImport     = require('postcss-import');
const nano          = require('cssnano');
const nesting       = require('postcss-nesting');
const sorting       = require('postcss-sorting');

const browserSync   = require('browser-sync').create();


gulp.task('clean', () => {
  return del('dist');
});

gulp.task('copy', () => {
  return gulp.src('src/events.json')
    .pipe(gulp.dest('dist'));
});

gulp.task('js', () => {
  return gulp.src('src/js/**')
    .pipe(gulp.dest('dist/js'))
    .on('end', browserSync.reload);
});

gulp.task('css', () => {
  return gulp.src('src/css/main.css')
    .pipe(plumber())
    .pipe(postcss([cssImport(), autoprefixer(), sorting(), nesting()]))
    .pipe(gulp.dest('dist/css'))
    .pipe(filter('**/*.css'))
    .pipe(postcss([nano()]))
    .pipe(rename({suffix: '.min'}))
    .pipe(gulp.dest('dist/css'))
    .on('end', browserSync.reload);
});

gulp.task('html', () => {
  return gulp.src('src/index.html')
    .pipe(gulp.dest('dist'))
    .on('end', browserSync.reload);
});

gulp.task('build', (cb) => {
  runSequence(
    'clean',
    ['copy', 'js', 'css', 'html'],
    cb
  );
});

gulp.task('server', () => {
  return browserSync.init({
    server: 'dist'
  });
});

gulp.task('watch', () => {
  gulp.watch('src/js/**', ['js']);
  gulp.watch('src/css/**', ['css']);
  gulp.watch('src/**/*.html', ['html']);
});

gulp.task('default', () => {
  runSequence('build', 'server', 'watch');
});
