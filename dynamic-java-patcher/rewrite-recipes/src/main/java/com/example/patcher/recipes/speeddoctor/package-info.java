/**
 * SpeedDoctor recipes for optimizing, modernizing, and securing Java applications.
 * <p>
 * This package contains three powerful OpenRewrite recipes:
 * <ul>
 *   <li><strong>HotspotRecipe</strong>: Selectively applies optimizations only to classes identified as performance 
 *       hotspots by the SpeedDoctor profiler.</li>
 *   <li><strong>ApiModernizationRecipe</strong>: Automatically replaces calls to deprecated APIs with their 
 *       modern counterparts.</li>
 *   <li><strong>SecurityPatchRecipe</strong>: Adds sanitization to vulnerable methods to prevent common security 
 *       issues like SQL injection and XSS attacks.</li>
 * </ul>
 * <p>
 * These recipes can be used alone or combined with the SpeedDoctor Byte Buddy agent for a complete 
 * runtime and build-time solution.
 * <p>
 * Example Maven configuration:
 * <pre>
 * &lt;plugin&gt;
 *   &lt;groupId&gt;org.openrewrite.maven&lt;/groupId&gt;
 *   &lt;artifactId&gt;rewrite-maven-plugin&lt;/artifactId&gt;
 *   &lt;version&gt;4.46.0&lt;/version&gt;
 *   &lt;configuration&gt;
 *     &lt;activeRecipes&gt;
 *       &lt;recipe&gt;com.example.patcher.recipes.speeddoctor.HotspotRecipe&lt;/recipe&gt;
 *       &lt;recipe&gt;com.example.patcher.recipes.speeddoctor.ApiModernizationRecipe&lt;/recipe&gt;
 *       &lt;recipe&gt;com.example.patcher.recipes.speeddoctor.SecurityPatchRecipe&lt;/recipe&gt;
 *     &lt;/activeRecipes&gt;
 *   &lt;/configuration&gt;
 * &lt;/plugin&gt;
 * </pre>
 * 
 * @see com.example.patcher.recipes.speeddoctor.HotspotRecipe
 * @see com.example.patcher.recipes.speeddoctor.ApiModernizationRecipe
 * @see com.example.patcher.recipes.speeddoctor.SecurityPatchRecipe
 */
package com.example.patcher.recipes.speeddoctor; 